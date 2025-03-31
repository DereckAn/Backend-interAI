package dereck.angeles.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "Feedback")
@Table(name = "feedback")
public class Feedback {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "interview_id")
    private Interview interview;

    @Column(name = "technical_score")
    private Integer technicalScore;

    @Column(name = "non_technical_score")
    private Integer nonTechnicalScore;

    @Column(name = "posture_notes", length = Integer.MAX_VALUE)
    private String postureNotes;

    @Column(name = "voice_tone_notes", length = Integer.MAX_VALUE)
    private String voiceToneNotes;

    @Column(name = "clothing_notes", length = Integer.MAX_VALUE)
    private String clothingNotes;

    @Column(name = "general_comments", length = Integer.MAX_VALUE)
    private String generalComments;

    @Column(name = "improvement_tips")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> improvementTips;

    @Column(name = "would_hire")
    private Boolean wouldHire;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}