package dereck.angeles.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class QuestionDTO {
    private UUID id;
    private UUID topicId;
    private UUID languageId;
    private UUID difficultyId;
    private String questionText;
    private String sampleAnswer;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}