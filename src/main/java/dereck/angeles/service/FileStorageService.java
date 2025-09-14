package dereck.angeles.service;

import dereck.angeles.dto.FileDto;
import dereck.angeles.dto.FileUploadResponseDto;
import dereck.angeles.model.File;
import dereck.angeles.model.User;
import dereck.angeles.repository.FileRepository;
import dereck.angeles.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.*;

@ApplicationScoped
public class FileStorageService {

    // Enterprise Bucket Configuration
    @ConfigProperty(name = "minio.bucket.user-resumes")
    String userResumesBucket;
    
    @ConfigProperty(name = "minio.bucket.interview-recordings")
    String interviewRecordingsBucket;
    
    @ConfigProperty(name = "minio.bucket.user-avatars")
    String userAvatarsBucket;
    
    @ConfigProperty(name = "minio.bucket.system-assets")
    String systemAssetsBucket;
    
    @ConfigProperty(name = "minio.bucket.temp-uploads")
    String tempUploadsBucket;

    @ConfigProperty(name = "app.file.upload.max-size", defaultValue = "10485760") // 10MB in bytes
    Long maxFileSize;

    @Inject
    FileRepository fileRepository;
    
    @Inject
    UserRepository userRepository;

    @Inject
    public S3Client s3Client;

    private static final Set<String> ALLOWED_RESUME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final Set<String> ALLOWED_JOB_DESC_TYPES = Set.of(
            "text/plain",
            "application/pdf"
    );

    @PostConstruct
    public void init() {
        // Test S3 client connection and create all enterprise buckets
        testS3Connection();
        createEnterpriseBuckets();
    }

    private void testS3Connection() {
        try {
            System.out.println("🔍 Testing S3/MinIO connection...");
            // Test connection by listing buckets
            ListBucketsResponse response = s3Client.listBuckets();
            System.out.println("✅ S3/MinIO connection successful");
            System.out.println("📦 Found " + response.buckets().size() + " existing buckets:");
            for (software.amazon.awssdk.services.s3.model.Bucket bucket : response.buckets()) {
                System.out.println("  - " + bucket.name());
            }
        } catch (Exception e) {
            System.err.println("❌ S3/MinIO connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createEnterpriseBuckets() {
        System.out.println("🏗️ Initializing enterprise bucket architecture...");
        
        // List of all enterprise buckets
        String[] buckets = {
            userResumesBucket,
            interviewRecordingsBucket,
            userAvatarsBucket,
            systemAssetsBucket,
            tempUploadsBucket
        };
        
        for (String bucketName : buckets) {
            createBucketIfNotExists(bucketName);
        }
        
        System.out.println("✅ Enterprise bucket architecture initialized successfully");
    }
    
    private void createBucketIfNotExists(String bucketName) {
        try {
            System.out.println("🔍 Checking if bucket '" + bucketName + "' exists...");
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            System.out.println("✅ Bucket '" + bucketName + "' already exists");
        } catch (NoSuchBucketException e) {
            try {
                System.out.println("🔧 Creating bucket '" + bucketName + "'...");
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                System.out.println("✅ Successfully created bucket '" + bucketName + "'");
            } catch (Exception createException) {
                System.err.println("❌ Failed to create bucket '" + bucketName + "': " + createException.getMessage());
                createException.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("❌ Error checking bucket '" + bucketName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Enterprise method: Select appropriate bucket based on file type
     */
    private String getBucketForFileType(File.FileType fileType) {
        return switch (fileType) {
            case RESUME -> userResumesBucket;
            case INTERVIEW_RECORDING -> interviewRecordingsBucket;
            case USER_AVATAR -> userAvatarsBucket;
            case SYSTEM_ASSET -> systemAssetsBucket;
            case TEMP_FILE -> tempUploadsBucket;
            default -> userResumesBucket; // Fallback
        };
    }
    
    /**
     * Enterprise method: Generate hierarchical object key with user prefix
     */
    private String generateEnterpriseObjectKey(File.FileType fileType, String userId, String filename) {
        String timestamp = java.time.Instant.now().toString().replaceAll("[:.]", "-");
        
        return switch (fileType) {
            case RESUME -> String.format("users/%s/resumes/%s_%s", userId, timestamp, filename);
            case INTERVIEW_RECORDING -> String.format("interviews/%s/recordings/%s_%s", userId, timestamp, filename);
            case USER_AVATAR -> String.format("users/%s/avatars/%s_%s", userId, timestamp, filename);
            case SYSTEM_ASSET -> String.format("system/assets/%s_%s", timestamp, filename);
            case TEMP_FILE -> String.format("temp/%s/%s_%s", userId, timestamp, filename);
            default -> String.format("misc/%s/%s_%s", userId, timestamp, filename);
        };
    }

    @Transactional
    public FileUploadResponseDto uploadFile(
            InputStream fileInputStream,
            String originalFilename,
            String contentType,
            Long fileSize,
            File.FileType fileType,
            String userId) {

        try {
            // Validate file
            FileUploadResponseDto validationResult = validateFile(originalFilename, contentType, fileSize, fileType);
            if (!validationResult.success()) {
                return validationResult;
            }

            // Enterprise: Select appropriate bucket and generate hierarchical object key
            String targetBucket = getBucketForFileType(fileType);
            String enterpriseObjectKey = generateEnterpriseObjectKey(fileType, userId, originalFilename);

            // Upload to MinIO with enterprise organization
            System.out.println("🏗️ Enterprise Upload - Bucket: " + targetBucket + ", Key: " + enterpriseObjectKey + " (" + fileSize + " bytes)");
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(enterpriseObjectKey)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            PutObjectResponse putResponse = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fileInputStream, fileSize));
            System.out.println("✅ File uploaded successfully to MinIO. ETag: " + putResponse.eTag());

            // Get user entity
            UUID userUuid = UUID.fromString(userId);
            User user = userRepository.findById(userUuid);
            if (user == null) {
                return FileUploadResponseDto.error("User not found");
            }
            
            // Save metadata to database with enterprise organization
            File file = File.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(enterpriseObjectKey) // Hierarchical path with user prefix
                    .contentType(contentType)
                    .fileSize(fileSize)
                    .bucketName(targetBucket) // Appropriate enterprise bucket
                    .fileType(fileType)
                    .user(user)
                    .build();

            fileRepository.persist(file);

            String downloadUrl = generateDownloadUrl(file.getId().toString());
            FileDto fileDto = FileDto.fromEntity(file, downloadUrl);

            return FileUploadResponseDto.success(fileDto);

        } catch (Exception e) {
            return FileUploadResponseDto.error("Failed to upload file: " + e.getMessage());
        }
    }

    public Optional<File> getFileById(String fileId) {
        UUID uuid = UUID.fromString(fileId);
        return fileRepository.findByIdOptional(uuid);
    }

    public InputStream downloadFile(String fileId) {
        UUID uuid = UUID.fromString(fileId);
        Optional<File> fileOptional = fileRepository.findByIdOptional(uuid);
        
        if (fileOptional.isEmpty()) {
            throw new RuntimeException("File not found: " + fileId);
        }
        
        File file = fileOptional.get();
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(file.getBucketName()) // Use stored bucket name (enterprise or legacy)
                .key(file.getStoredFilename()) // Use stored key (hierarchical or legacy)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    @Transactional
    public boolean deleteFile(String fileId) {
        UUID uuid = UUID.fromString(fileId);
        Optional<File> fileOptional = fileRepository.findByIdOptional(uuid);
        
        if (fileOptional.isPresent()) {
            File file = fileOptional.get();
            
            try {
                // Delete from MinIO (works with both enterprise and legacy buckets)
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(file.getBucketName()) // Use stored bucket name
                        .key(file.getStoredFilename()) // Use stored key
                        .build();
                
                s3Client.deleteObject(deleteObjectRequest);
                
                // Delete from database
                fileRepository.delete(file);
                
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }

    public List<FileDto> getUserFiles(String userId) {
        return fileRepository.findByUserId(userId)
                .stream()
                .map(file -> {
                    String downloadUrl = generateDownloadUrl(file.getId().toString());
                    return FileDto.fromEntity(file, downloadUrl);
                })
                .toList();
    }

    public List<FileDto> getUserFilesByType(String userId, File.FileType fileType) {
        return fileRepository.findByUserIdAndFileType(userId, fileType)
                .stream()
                .map(file -> {
                    String downloadUrl = generateDownloadUrl(file.getId().toString());
                    return FileDto.fromEntity(file, downloadUrl);
                })
                .toList();
    }

    private FileUploadResponseDto validateFile(String filename, String contentType, Long fileSize, File.FileType fileType) {
        System.out.println("🔍 Validating file: " + filename + " (type: " + contentType + ", size: " + fileSize + " bytes, fileType: " + fileType + ")");
        
        // Check file size
        if (fileSize > maxFileSize) {
            System.err.println("❌ File size validation failed: " + fileSize + " > " + maxFileSize);
            return FileUploadResponseDto.error("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Check content type based on file type
        Set<String> allowedTypes = fileType == File.FileType.RESUME ? ALLOWED_RESUME_TYPES : ALLOWED_JOB_DESC_TYPES;
        System.out.println("📋 Allowed types for " + fileType + ": " + allowedTypes);
        
        // Extract the base content type (without charset or other parameters)
        String baseContentType = contentType.split(";")[0].trim();
        System.out.println("🔍 Base content type: '" + baseContentType + "' (original: '" + contentType + "')");
        
        if (!allowedTypes.contains(baseContentType)) {
            System.err.println("❌ Content type validation failed: '" + baseContentType + "' not in " + allowedTypes);
            return FileUploadResponseDto.error("File type not allowed for " + fileType.name().toLowerCase() + ". Received: " + baseContentType);
        }

        // Check filename
        if (filename == null || filename.trim().isEmpty()) {
            System.err.println("❌ Filename validation failed: empty filename");
            return FileUploadResponseDto.error("Filename cannot be empty");
        }

        System.out.println("✅ File validation passed for " + filename);
        return FileUploadResponseDto.success(null);
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + extension;
    }

    private String generateDownloadUrl(String fileId) {
        return "/api/files/" + fileId + "/download";
    }
}