package ru.manager.ProgectManager.entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 6_291_456)
    @Lob
    private byte[] photo;

    @JsonIgnore
    @OneToMany
    private List<UserWithProjectConnector> connectors;

    @JsonIgnore
    @OneToMany
    private List<KanbanColumn> kanbanColumns;
}
