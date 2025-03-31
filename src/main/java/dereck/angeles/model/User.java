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
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Column(name = "\"emailVerified\"")
    private OffsetDateTime emailVerified;

    @Column(name = "image", length = Integer.MAX_VALUE)
    private String image;

    @Size(max = 100)
    @Column(name = "username", length = 100)
    private String username;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "user")
    private Set<Account> accounts = new LinkedHashSet<>();
    @OneToMany(mappedBy = "user")
    private Set<Interview> interviews = new LinkedHashSet<>();

/*
 TODO [Reverse Engineering] create field to map the 'role' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @ColumnDefault("'USER'")
    @Column(name = "role", columnDefinition = "authrole not null")
    private Object role;
*/
}