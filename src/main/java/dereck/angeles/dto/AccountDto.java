package dereck.angeles.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link dereck.angeles.model.Account}
 */
public record AccountDto(
			UUID id, @NotNull @Size(max = 255) String type,
			@NotNull @Size(max = 255) String provider,
			@NotNull @Size(max = 255) String providerAccountId,
			String refreshToken, String accessToken,
			Long expiresAt, String idToken, String scope,
			String sessionState,
			String tokenType)
			implements Serializable {
}