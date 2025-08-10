package dereck.angeles.repository;

import dereck.angeles.model.Topic;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class TopicRepository implements PanacheRepositoryBase<Topic, UUID> {

    public Topic findByName(String name) {
        return find("name", name).firstResult();
    }
}