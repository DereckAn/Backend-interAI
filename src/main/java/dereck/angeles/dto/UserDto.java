package dereck.angeles.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for {@link dereck.angeles.model.User}
 */
public record UserDto(
			UUID id, @Size(max = 255) String name, @Size(max = 255) String email,
			OffsetDateTime emailVerified, String image,
			Instant createdAt,
			Set<AccountDto> accounts, Set<InterviewDto> interviews)
			implements Serializable {
}