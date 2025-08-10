package dereck.angeles.repository;

import dereck.angeles.model.Language;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class LanguageRepository implements PanacheRepositoryBase<Language, UUID> {
    
    public Language findByName(String name) {
        return find("name", name).firstResult();
    }
}
