package dereck.angeles.repository;

import dereck.angeles.model.Topic;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TopicRepository implements PanacheRepository<Topic> {
    
    public Topic findByName(String name) {
        return find("name", name).firstResult();
    }
}