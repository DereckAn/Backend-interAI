package dereck.angeles.service;

import dereck.angeles.dto.FileDto;
import dereck.angeles.dto.FileUploadResponseDto;
import dereck.angeles.model.File;
import dereck.angeles.repository.FileRepository;
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

    @ConfigProperty(name = "minio.bucket-name")
    String bucketName;

    @ConfigProperty(name = "app.file.upload.max-size", defaultValue = "10485760") // 10MB in bytes
    Long maxFileSize;

    @Inject
    FileRepository fileRepository;

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
        // Create bucket if it doesn't exist
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
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

            // Generate unique filename
            String storedFilename = generateUniqueFilename(originalFilename);

            // Upload to MinIO
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedFilename)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fileInputStream, fileSize));

            // Save metadata to database
            File file = File.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .contentType(contentType)
                    .fileSize(fileSize)
                    .bucketName(bucketName)
                    .fileType(fileType)
                    .userId(userId)
                    .build();

            fileRepository.persist(file);

            String downloadUrl = generateDownloadUrl(file.getId());
            FileDto fileDto = FileDto.fromEntity(file, downloadUrl);

            return FileUploadResponseDto.success(fileDto);

        } catch (Exception e) {
            return FileUploadResponseDto.error("Failed to upload file: " + e.getMessage());
        }
    }

    public Optional<File> getFileById(String fileId) {
        return fileRepository.findByIdOptional(fileId);
    }

    public InputStream downloadFile(String storedFilename) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(storedFilename)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    @Transactional
    public boolean deleteFile(String fileId) {
        Optional<File> fileOptional = fileRepository.findByIdOptional(fileId);
        
        if (fileOptional.isPresent()) {
            File file = fileOptional.get();
            
            try {
                // Delete from MinIO
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getStoredFilename())
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
                    String downloadUrl = generateDownloadUrl(file.getId());
                    return FileDto.fromEntity(file, downloadUrl);
                })
                .toList();
    }

    public List<FileDto> getUserFilesByType(String userId, File.FileType fileType) {
        return fileRepository.findByUserIdAndFileType(userId, fileType)
                .stream()
                .map(file -> {
                    String downloadUrl = generateDownloadUrl(file.getId());
                    return FileDto.fromEntity(file, downloadUrl);
                })
                .toList();
    }

    private FileUploadResponseDto validateFile(String filename, String contentType, Long fileSize, File.FileType fileType) {
        // Check file size
        if (fileSize > maxFileSize) {
            return FileUploadResponseDto.error("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Check content type based on file type
        Set<String> allowedTypes = fileType == File.FileType.RESUME ? ALLOWED_RESUME_TYPES : ALLOWED_JOB_DESC_TYPES;
        
        if (!allowedTypes.contains(contentType)) {
            return FileUploadResponseDto.error("File type not allowed for " + fileType.name().toLowerCase());
        }

        // Check filename
        if (filename == null || filename.trim().isEmpty()) {
            return FileUploadResponseDto.error("Filename cannot be empty");
        }

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