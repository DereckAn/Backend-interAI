package dereck.angeles.dto;

import dereck.angeles.model.File;

import java.time.LocalDateTime;

public record FileDto(
        String id,
        String originalFilename,
        String contentType,
        Long fileSize,
        File.FileType fileType,
        String userId,
        LocalDateTime uploadDate,
        String downloadUrl
) {
    public static FileDto fromEntity(File file, String downloadUrl) {
        return new FileDto(
                file.getId(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getFileSize(),
                file.getFileType(),
                file.getUserId(),
                file.getUploadDate(),
                downloadUrl
        );
    }
    
    public static FileDto fromEntity(File file) {
        return fromEntity(file, null);
    }
}