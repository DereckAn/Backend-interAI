package dereck.angeles.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@Entity(name = "Account")
@Table(name = "accounts")
public class Account {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "\"userId\"", nullable = false)
    private dereck.angeles.model.User user;

    @Size(max = 255)
    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @Size(max = 255)
    @NotNull
    @Column(name = "provider", nullable = false)
    private String provider;

    @Size(max = 255)
    @NotNull
    @Column(name = "\"providerAccountId\"", nullable = false)
    private String providerAccountId;

    @Column(name = "refresh_token", length = Integer.MAX_VALUE)
    private String refreshToken;

    @Column(name = "access_token", length = Integer.MAX_VALUE)
    private String accessToken;

    @Column(name = "expires_at")
    private Long expiresAt;

    @Column(name = "id_token", length = Integer.MAX_VALUE)
    private String idToken;

    @Column(name = "scope", length = Integer.MAX_VALUE)
    private String scope;

    @Column(name = "session_state", length = Integer.MAX_VALUE)
    private String sessionState;

    @Column(name = "token_type", length = Integer.MAX_VALUE)
    private String tokenType;

}