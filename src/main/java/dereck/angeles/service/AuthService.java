package dereck.angeles.service;

import dereck.angeles.dto.LoginDto;
import dereck.angeles.dto.RegisterDto;
import dereck.angeles.model.User;
import dereck.angeles.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.Set;
import org.mindrot.jbcrypt.BCrypt;
import io.smallrye.jwt.build.Jwt;

@ApplicationScoped
public class AuthService {

	@Inject
	UserRepository userRepository;

	@Transactional
	public User register(RegisterDto registerDto) throws Exception {
		// Validate password match
		if (!registerDto.password().equals(registerDto.confirmPassword())) {
			throw new Exception("Passwords do not match");
		}

		// Check if email or username already exists
		if (userRepository.findByEmail(registerDto.email()) != null) {
			throw new Exception("Email already registered");
		}
		if (userRepository.findByUsername(registerDto.username()) != null) {
			throw new Exception("Username already taken");
		}

		// Create new user
		User user = new User();
		user.setName(registerDto.name());
		user.setEmail(registerDto.email());
		user.setUsername(registerDto.username());
		user.setPassword(BCrypt.hashpw(registerDto.password(), BCrypt.gensalt()));
		user.setRole(User.AuthRole.USER);
		userRepository.persist(user);

		return user;
	}

	public String login(LoginDto loginDto) throws Exception {
		User user = userRepository.findByEmail(loginDto.email());
		if (user == null) {
			throw new Exception("User not found");
		}

		// Verify password
		if (!BCrypt.checkpw(loginDto.password(), user.getPassword())) {
			throw new Exception("Invalid password");
		}

		// Generate JWT
		return Jwt.issuer("dereckan-interback")
							.subject(user.getId().toString())
							.groups(Set.of(user.getRole().toString()))
							.expiresIn(Duration.ofHours(24))
							.sign();
	}
}