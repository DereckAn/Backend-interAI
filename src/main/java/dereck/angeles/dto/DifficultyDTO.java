package dereck.angeles.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class DifficultyDTO {
    private UUID id;
    private String level;
    private String description;
}