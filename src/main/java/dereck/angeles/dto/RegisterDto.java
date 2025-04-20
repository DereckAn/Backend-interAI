package dereck.angeles.dto;

public record RegisterDto(
			String name,
			String email,
			String username,
			String password,
			String confirmPassword
) {}