package dereck.angeles.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "User")
@Table(name = "users")
public class User {
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Size(max = 255)
	@Column(name = "name")
	private String name;

	@Size(max = 255)
	@Column(name = "email", unique = true)
	private String email;

	@Column(name = "\"emailVerified\"")
	private OffsetDateTime emailVerified;

	@Size(max = 1000)
	@Column(name = "image", length = 1000)
	private String image;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private AuthRole role = AuthRole.USER;

	@Size(max = 100)
	@Column(name = "username", unique = true, length = 100)
	private String username;

	@Column(name = "password")
	private String password;

	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at")
	private Instant createdAt;

	@OneToMany(mappedBy = "user")
	private Set<Account> accounts = new LinkedHashSet<>();

	@OneToMany(mappedBy = "user")
	private Set<Interview> interviews = new LinkedHashSet<>();

	public enum AuthRole {
		USER, ADMIN
	}
}