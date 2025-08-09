package dereck.angeles.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "Interview")
@Table(name = "interviews")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "language_id")
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "difficulty_id")
    private Difficulty difficulty;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Size(max = 255)
    @Column(name = "video_url")
    private String videoUrl;

    @Size(max = 255)
    @Column(name = "audio_url")
    private String audioUrl;

    @Size(max = 50)
    @ColumnDefault("'in_progress'")
    @Column(name = "status", length = 50)
    private String status;

/*
 TODO [Reverse Engineering] create field to map the 'duration' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @ColumnDefault("(end_time - start_time)")
    @Column(name = "duration", columnDefinition = "interval")
    private Object duration;
*/
}