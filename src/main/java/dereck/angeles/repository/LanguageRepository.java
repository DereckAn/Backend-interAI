package dereck.angeles.repository;

import dereck.angeles.model.Language;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LanguageRepository implements PanacheRepository<Language>{
    
    public Language findByName(String name) {
        return find("name", name).firstResult();
    }
}
