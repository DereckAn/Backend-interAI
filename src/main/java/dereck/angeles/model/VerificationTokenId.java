package dereck.angeles.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class VerificationTokenId implements Serializable {
    private static final long serialVersionUID = 3413025765970464694L;
    @NotNull
    @Column(name = "identifier", nullable = false, length = Integer.MAX_VALUE)
    private String identifier;

    @NotNull
    @Column(name = "token", nullable = false, length = Integer.MAX_VALUE)
    private String token;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        VerificationTokenId entity = (VerificationTokenId) o;
        return Objects.equals(this.identifier, entity.identifier) &&
                Objects.equals(this.token, entity.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, token);
    }

}