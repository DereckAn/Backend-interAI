package dereck.angeles.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link dereck.angeles.model.Topic}
 */
public record TopicDto(
			@NotNull UUID id, @NotNull @Size(max = 100) String name,
			String description, Instant createdAt,
			Instant updatedAt)
			implements Serializable {
}