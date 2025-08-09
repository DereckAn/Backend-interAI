package dereck.angeles.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.ColumnDefault;
import com.fasterxml.jackson.annotation.JsonProperty;

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
	@Enumerated(EnumType.STRING)
	private DifficultyLevel level;

	@Column(name = "description", length = Integer.MAX_VALUE)
	private String description;

	@Column(name = "years_of_experience")
	private Integer yearsOfExperience;

	public enum DifficultyLevel {
		Junior,
		@JsonProperty("Mid-Level")
		MidLevel,
		Senior
	}

}