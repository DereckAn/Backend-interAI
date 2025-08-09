package dereck.angeles.controller;

import dereck.angeles.dto.FileDto;
import dereck.angeles.dto.FileUploadResponseDto;
import dereck.angeles.model.File;
import dereck.angeles.service.FileStorageService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/api/files")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class FileController {

    @Inject
    FileStorageService fileStorageService;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@MultipartForm MultipartFormDataInput input) {
        try {
            // Get authenticated user ID from JWT token
            if (securityIdentity.isAnonymous()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(FileUploadResponseDto.error("Authentication required"))
                        .build();
            }
            
            String userId = securityIdentity.getPrincipal().getName();
            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(FileUploadResponseDto.error("Invalid token: userId not found"))
                        .build();
            }
            
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

            // Get file from form
            List<InputPart> fileParts = uploadForm.get("file");
            if (fileParts == null || fileParts.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(FileUploadResponseDto.error("No file provided"))
                        .build();
            }

            InputPart filePart = fileParts.get(0);
            
            // Get file metadata
            String contentType = filePart.getMediaType().toString();
            String originalFilename = getFileName(filePart);
            
            // Get file type from form
            String fileTypeStr = getFormFieldValue(uploadForm, "fileType");
            
            if (fileTypeStr == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(FileUploadResponseDto.error("fileType is required"))
                        .build();
            }

            File.FileType fileType;
            try {
                fileType = File.FileType.valueOf(fileTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(FileUploadResponseDto.error("Invalid fileType. Must be RESUME or JOB_DESCRIPTION"))
                        .build();
            }

            // Get file input stream and size
            InputStream fileInputStream = filePart.getBody(InputStream.class, null);
            long fileSize = getFileSize(filePart);

            // Upload file
            FileUploadResponseDto result = fileStorageService.uploadFile(
                    fileInputStream,
                    originalFilename,
                    contentType,
                    fileSize,
                    fileType,
                    userId
            );

            if (result.success()) {
                return Response.ok(result).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(FileUploadResponseDto.error("Upload failed: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{fileId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("fileId") String fileId) {
        // Get authenticated user ID
        if (securityIdentity.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String authenticatedUserId = securityIdentity.getPrincipal().getName();
        
        Optional<File> fileOptional = fileStorageService.getFileById(fileId);
        
        if (fileOptional.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        File file = fileOptional.get();
        
        // Check if the file belongs to the authenticated user
        if (!file.getUser().getId().equals(authenticatedUserId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Access denied: file belongs to another user\"}")
                    .build();
        }

        try {
            InputStream fileStream = fileStorageService.downloadFile(file.getStoredFilename());
            
            StreamingOutput streamingOutput = output -> {
                try (InputStream inputStream = fileStream) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }
            };

            return Response.ok(streamingOutput)
                    .header("Content-Disposition", "attachment; filename=\"" + file.getOriginalFilename() + "\"")
                    .header("Content-Type", file.getContentType())
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to download file: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/debug/minio-objects")
    public Response listMinIOObjects() {
        try {
            if (securityIdentity.isAnonymous()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            
            var listRequest = software.amazon.awssdk.services.s3.model.ListObjectsV2Request.builder()
                    .bucket("interview-files")
                    .build();
            
            var response = fileStorageService.s3Client.listObjectsV2(listRequest);
            
            var objects = response.contents().stream()
                    .map(obj -> Map.of(
                        "key", obj.key(),
                        "size", obj.size(),
                        "lastModified", obj.lastModified().toString()
                    ))
                    .toList();
                    
            return Response.ok(objects).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/my-files")
    public Response getMyFiles() {
        // Get authenticated user ID
        if (securityIdentity.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String authenticatedUserId = securityIdentity.getPrincipal().getName();
        List<FileDto> files = fileStorageService.getUserFiles(authenticatedUserId);
        return Response.ok(files).build();
    }

    @GET
    @Path("/user/{userId}")
    public Response getUserFiles(@PathParam("userId") String userId) {
        // Get authenticated user ID
        if (securityIdentity.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String authenticatedUserId = securityIdentity.getPrincipal().getName();
        
        // Users can only access their own files
        if (!userId.equals(authenticatedUserId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Access denied: can only access your own files\"}")
                    .build();
        }
        
        List<FileDto> files = fileStorageService.getUserFiles(userId);
        return Response.ok(files).build();
    }

    @GET
    @Path("/my-files/type/{fileType}")
    public Response getMyFilesByType(@PathParam("fileType") String fileTypeStr) {
        // Get authenticated user ID
        if (securityIdentity.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String authenticatedUserId = securityIdentity.getPrincipal().getName();
        
        try {
            File.FileType fileType = File.FileType.valueOf(fileTypeStr.toUpperCase());
            List<FileDto> files = fileStorageService.getUserFilesByType(authenticatedUserId, fileType);
            return Response.ok(files).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid fileType. Must be RESUME or JOB_DESCRIPTION")
                    .build();
        }
    }

    @GET
    @Path("/user/{userId}/type/{fileType}")
    public Response getUserFilesByType(
            @PathParam("userId") String userId,
            @PathParam("fileType") String fileTypeStr) {
        
        // Get authenticated user ID
        if (securityIdentity.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String authenticatedUserId = securityIdentity.getPrincipal().getName();
        
        // Users can only access their own files
        if (!userId.equals(authenticatedUserId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Access denied: can only access your own files\"}")
                    .build();
        }
        
        try {
            File.FileType fileType = File.FileType.valueOf(fileTypeStr.toUpperCase());
            List<FileDto> files = fileStorageService.getUserFilesByType(userId, fileType);
            return Response.ok(files).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid fileType. Must be RESUME or JOB_DESCRIPTION")
                    .build();
        }
    }

    @DELETE
    @Path("/{fileId}")
    public Response deleteFile(@PathParam("fileId") String fileId) {
        // Get authenticated user ID
        if (securityIdentity.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String authenticatedUserId = securityIdentity.getPrincipal().getName();
        
        // Check if file exists and belongs to authenticated user
        Optional<File> fileOptional = fileStorageService.getFileById(fileId);
        if (fileOptional.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"File not found\"}")
                    .build();
        }
        
        File file = fileOptional.get();
        if (!file.getUser().getId().equals(authenticatedUserId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Access denied: can only delete your own files\"}")
                    .build();
        }
        
        boolean deleted = fileStorageService.deleteFile(fileId);
        
        if (deleted) {
            return Response.ok().entity("{\"message\": \"File deleted successfully\"}").build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Could not delete file\"}")
                    .build();
        }
    }

    @GET
    @Path("/{fileId}")
    public Response getFileMetadata(@PathParam("fileId") String fileId) {
        // Get authenticated user ID
        if (securityIdentity.isAnonymous()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String authenticatedUserId = securityIdentity.getPrincipal().getName();
        
        Optional<File> fileOptional = fileStorageService.getFileById(fileId);
        
        if (fileOptional.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        File file = fileOptional.get();
        
        // Check if the file belongs to the authenticated user
        if (!file.getUser().getId().equals(authenticatedUserId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Access denied: file belongs to another user\"}")
                    .build();
        }

        String downloadUrl = "/api/files/" + fileId + "/download";
        FileDto fileDto = FileDto.fromEntity(file, downloadUrl);
        
        return Response.ok(fileDto).build();
    }

    private String getFileName(InputPart part) {
        String contentDisposition = part.getHeaders().getFirst("Content-Disposition");
        if (contentDisposition != null) {
            for (String param : contentDisposition.split(";")) {
                if (param.trim().startsWith("filename")) {
                    String filename = param.substring(param.indexOf('=') + 1).trim();
                    return filename.replaceAll("\"", "");
                }
            }
        }
        return "unknown";
    }

    private long getFileSize(InputPart part) {
        try {
            return part.getBody(InputStream.class, null).available();
        } catch (IOException e) {
            return 0;
        }
    }

    private String getFormFieldValue(Map<String, List<InputPart>> formData, String fieldName) {
        List<InputPart> parts = formData.get(fieldName);
        if (parts != null && !parts.isEmpty()) {
            try {
                return parts.get(0).getBodyAsString();
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }
}