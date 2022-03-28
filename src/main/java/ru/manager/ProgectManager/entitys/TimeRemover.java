package ru.manager.ProgectManager.entitys;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class TimeRemover {
    @Id
    private long removerId;

    @Column(nullable = false)
    private long timeToDelete;

    @Column(nullable = false)
    private boolean hard;
}
