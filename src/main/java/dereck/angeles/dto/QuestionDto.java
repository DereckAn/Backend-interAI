package dereck.angeles.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link dereck.angeles.model.Question}
 */
public record QuestionDto(
			UUID id, @NotNull String questionText, String sampleAnswer,
			Instant createdAt,
			Instant updatedAt)
			implements Serializable {
}