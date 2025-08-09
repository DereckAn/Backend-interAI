package dereck.angeles.repository;

import dereck.angeles.model.Difficulty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DifficultyRepository {

    @Inject
    EntityManager entityManager;

    public Difficulty findByLevel(String level) {
        // Handle the mapping from frontend string to enum
        Difficulty.DifficultyLevel difficultyLevel;
        switch (level) {
            case "Junior":
                difficultyLevel = Difficulty.DifficultyLevel.Junior;
                break;
            case "Mid-Level":
                difficultyLevel = Difficulty.DifficultyLevel.MidLevel;
                break;
            case "Senior":
                difficultyLevel = Difficulty.DifficultyLevel.Senior;
                break;
            default:
                throw new IllegalArgumentException("Invalid difficulty level: " + level);
        }
        
        List<Difficulty> difficulties = entityManager
                .createQuery("SELECT d FROM Difficulty d WHERE d.level = :level", Difficulty.class)
                .setParameter("level", difficultyLevel)
                .getResultList();
        return difficulties.isEmpty() ? null : difficulties.get(0);
    }

    public Difficulty findById(UUID id) {
        return entityManager.find(Difficulty.class, id);
    }

    public void persist(Difficulty difficulty) {
        entityManager.persist(difficulty);
    }

    public List<Difficulty> findAll() {
        return entityManager
                .createQuery("SELECT d FROM Difficulty d", Difficulty.class)
                .getResultList();
    }
}