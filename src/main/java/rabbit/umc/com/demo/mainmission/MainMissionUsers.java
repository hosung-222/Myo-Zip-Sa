package rabbit.umc.com.demo.mainmission;

import lombok.Getter;
import lombok.Setter;
import rabbit.umc.com.demo.Status;
import rabbit.umc.com.demo.user.User;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "main_mission_users")
public class MainMissionUsers {
    @Id @GeneratedValue
    @Column(name = "main_mission_user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private MainMission mainMission;

    private int score;

    @Enumerated(EnumType.STRING)
    private Status status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
