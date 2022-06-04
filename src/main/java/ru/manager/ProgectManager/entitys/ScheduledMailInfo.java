package ru.manager.ProgectManager.entitys;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class ScheduledMailInfo {
    @Id
    private String userEmail;

    private boolean resend;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 512)
    private String text;
}
