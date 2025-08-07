package dereck.angeles.repository;

import dereck.angeles.model.File;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FileRepository implements PanacheRepositoryBase<File, String> {
    
    public List<File> findByUserId(String userId) {
        return find("userId", userId).list();
    }
    
    public List<File> findByUserIdAndFileType(String userId, File.FileType fileType) {
        return find("userId = ?1 and fileType = ?2", userId, fileType).list();
    }
    
    public Optional<File> findByStoredFilename(String storedFilename) {
        return find("storedFilename", storedFilename).firstResultOptional();
    }
    
    public boolean existsByStoredFilename(String storedFilename) {
        return find("storedFilename", storedFilename).count() > 0;
    }
}