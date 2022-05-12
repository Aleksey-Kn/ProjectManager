package ru.manager.ProgectManager.entitys.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Note {
    @Id
    @JsonIgnore
    private long userId;

    @Column(nullable = false)
    private String text;
}
