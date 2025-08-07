package dereck.angeles.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    
    @Column(name = "stored_filename", nullable = false, unique = true)
    private String storedFilename;
    
    @Column(name = "content_type", nullable = false)
    private String contentType;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "bucket_name", nullable = false)
    private String bucketName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;
    
    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
    
    public enum FileType {
        RESUME,
        JOB_DESCRIPTION
    }
}