package dereck.angeles.repository;

import dereck.angeles.model.File;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FileRepository implements PanacheRepositoryBase<File, UUID> {
    
    public List<File> findByUserId(String userId) {
        UUID userUuid = UUID.fromString(userId);
        return find("user.id", userUuid).list();
    }
    
    public List<File> findByUserIdAndFileType(String userId, File.FileType fileType) {
        UUID userUuid = UUID.fromString(userId);
        return find("user.id = ?1 and fileType = ?2", userUuid, fileType).list();
    }
    
    public Optional<File> findByStoredFilename(String storedFilename) {
        return find("storedFilename", storedFilename).firstResultOptional();
    }
    
    public boolean existsByStoredFilename(String storedFilename) {
        return find("storedFilename", storedFilename).count() > 0;
    }
}