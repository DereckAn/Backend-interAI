package dereck.angeles.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.UUID;

@Data
public class InterviewDTO {
    private UUID id;
    private UUID userId;
    private UUID topicId;
    private UUID languageId;
    private UUID difficultyId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Duration duration;
    private String videoUrl;
    private String audioUrl;
    private String status;
}