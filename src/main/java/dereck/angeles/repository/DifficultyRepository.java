package dereck.angeles.repository;

import dereck.angeles.model.Difficulty;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DifficultyRepository implements PanacheRepositoryBase<Difficulty, UUID> {

    public Difficulty findByLevel(String level) {
        // Query by level name string as stored in database instead of enum
        List<Difficulty> difficulties = getEntityManager()
                .createQuery("SELECT d FROM Difficulty d WHERE CAST(d.level AS STRING) = :level", Difficulty.class)
                .setParameter("level", level)
                .getResultList();
        return difficulties.isEmpty() ? null : difficulties.get(0);
    }
}