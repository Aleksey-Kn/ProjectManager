package ru.manager.ProgectManager.entitys;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
@Getter
@Setter
public class ApproveEnabledUser {
    @Id
    private String token;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
