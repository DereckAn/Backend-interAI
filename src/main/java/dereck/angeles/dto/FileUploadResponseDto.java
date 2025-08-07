package dereck.angeles.dto;

public record FileUploadResponseDto(
        boolean success,
        String message,
        FileDto fileDto,
        String error
) {
    public static FileUploadResponseDto success(FileDto fileDto) {
        return new FileUploadResponseDto(
                true,
                "File uploaded successfully",
                fileDto,
                null
        );
    }
    
    public static FileUploadResponseDto error(String error) {
        return new FileUploadResponseDto(
                false,
                "File upload failed",
                null,
                error
        );
    }
}