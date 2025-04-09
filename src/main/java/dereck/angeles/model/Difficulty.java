package dereck.angeles.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Getter
@Setter
@Entity(name = "Difficulty")
@Table(name = "difficulties")
public class Difficulty {
	@Id
	@ColumnDefault("uuid_generate_v4()")
	@Column(name = "id", nullable = false)
	private UUID id;

	@Size(max = 50)
	@NotNull
	@Column(name = "level", nullable = false, length = 50)
	private DifficultyLevel level;

	@Column(name = "description", length = Integer.MAX_VALUE)
	private String description;

	@Column(name = "years_of_experience")
	private Integer yearsOfExperience;

	public enum DifficultyLevel {
		Junior,
		MidLevel,
		Senior
	}

}