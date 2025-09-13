package dereck.angeles.repository;

import dereck.angeles.model.Difficulty;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DifficultyRepository implements PanacheRepositoryBase<Difficulty, UUID> {

    public Difficulty findByLevel(String level) {
        // Convert frontend level string to enum for database lookup
        Difficulty.DifficultyLevel enumLevel;
        
        switch (level) {
            case "Junior":
                enumLevel = Difficulty.DifficultyLevel.Junior;
                break;
            case "Mid-Level":  // Frontend sends "Mid-Level"
                enumLevel = Difficulty.DifficultyLevel.MidLevel;  // But enum is "MidLevel"
                break;
            case "Senior":
                enumLevel = Difficulty.DifficultyLevel.Senior;
                break;
            default:
                return null;
        }
        
        // Query by enum value directly
        List<Difficulty> difficulties = find("level", enumLevel).list();
        return difficulties.isEmpty() ? null : difficulties.get(0);
    }
}