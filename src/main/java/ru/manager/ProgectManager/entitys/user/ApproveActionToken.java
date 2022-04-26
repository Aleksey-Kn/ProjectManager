package ru.manager.ProgectManager.entitys.user;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.enums.ActionType;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class ApproveActionToken {
    @Id
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated
    private ActionType actionType;
}
