package dereck.angeles.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private OffsetDateTime emailVerified;
    private String image;
    private String role;
    private String username;
    private OffsetDateTime createdAt;
}