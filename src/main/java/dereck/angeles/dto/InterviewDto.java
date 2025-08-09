package dereck.angeles.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link dereck.angeles.model.Interview}
 */
public record InterviewDto(
		UUID id,
		String userId,
		String topicName,
		String languageName,
		String difficultyLevel,
		String jobDescription,
		Integer experienceYears,
		Instant startTime,
		Instant endTime,
		@Size(max = 255) String videoUrl,
		@Size(max = 255) String audioUrl,
		@Size(max = 50) String status) implements Serializable {
}