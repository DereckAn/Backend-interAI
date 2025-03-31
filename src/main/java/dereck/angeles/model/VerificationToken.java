package dereck.angeles.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity(name = "VerificationToken")
@Table(name = "verification_token")
public class VerificationToken {
    @EmbeddedId
    private VerificationTokenId id;

    @NotNull
    @Column(name = "expires", nullable = false)
    private OffsetDateTime expires;

}