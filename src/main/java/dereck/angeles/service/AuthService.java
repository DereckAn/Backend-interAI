package dereck.angeles.service;

import dereck.angeles.dto.LoginDto;
import dereck.angeles.dto.RegisterDto;
import dereck.angeles.model.User;
import dereck.angeles.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;
import io.smallrye.jwt.build.Jwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AuthService {
	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	@Inject
	UserRepository userRepository;

	@Transactional
	public User register(RegisterDto registerDto) throws Exception {
		if (!registerDto.password().equals(registerDto.confirmPassword())) {
			throw new Exception("Passwords do not match");
		}

		if (userRepository.findByEmail(registerDto.email()) != null) {
			throw new Exception("Email already registered");
		}

		if (userRepository.findByUsername(registerDto.username()) != null) {
			throw new Exception("Username already taken");
		}

		User user = new User();
		user.setName(registerDto.name());
		user.setEmail(registerDto.email());
		user.setUsername(registerDto.username());
		user.setPassword(BCrypt.hashpw(registerDto.password(), BCrypt.gensalt()));
		user.setRole(User.AuthRole.USER);
		user.setCreatedAt(Instant.now());
		userRepository.persist(user);

		return user;
	}

	public LoginResponse login(LoginDto loginDto) throws Exception {
		// Log the incoming login request (be careful not to log passwords!)
		logger.info("Login attempt for email: {}", loginDto.email());

		User user = userRepository.findByEmail(loginDto.email());
		if (user == null) {
			logger.warn("Login failed: User not found for email: {}",
									loginDto.email());
			throw new Exception("User not found");
		}

		logger.info("User found: ID={}, Username={}", user.getId(),
								user.getUsername());

		if (!BCrypt.checkpw(loginDto.password(), user.getPassword())) {
			logger.warn("Login failed: Invalid password for user: {}",
									user.getUsername());
			throw new Exception("Invalid password");
		}

		logger.info("Password verification successful for user: {}",
								user.getUsername());
		logger.info("password with hash: {}", user.getPassword());
		logger.info("password without hash: {}", loginDto.password());
		logger.info("are the password the same?: {}",
								BCrypt.checkpw(loginDto.password(), user.getPassword()));

		String token;
		try {
			token = Jwt.issuer("dereckan-interback")
								 .subject(user.getId().toString())
								 .groups(Set.of(user.getRole().toString()))
								 .expiresIn(Duration.ofHours(24))
								 .sign();
			logger.info("Generating JWT token for user: {}", user.getUsername());
		} catch (Exception e) {
			logger.error("Error generating JWT token: {}", e.getMessage());
			throw new Exception("Error generating JWT token");
		}

//		logger.info("JWT token generated: {}", token);

		logger.info("JWT token generated successfully for user: {}",
								user.getUsername());

		return new LoginResponse(token, user.getId().toString());
	}

	public User getUserById(UUID userId) {
		return userRepository.findById(userId);
	}

	@Setter
	@Getter
	public static class LoginResponse {
		private String token;
		private String userId;

		public LoginResponse(String token, String userId) {
			this.token = token;
			this.userId = userId;
		}

	}
}