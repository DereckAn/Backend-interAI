package dereck.angeles.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link dereck.angeles.model.Language}
 */
public record LanguageDto(
			UUID id, @NotNull @Size(max = 100) String name, Instant createdAt,
			Instant updatedAt)
			implements Serializable {
}