package ru.manager.ProgectManager.entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Kanban {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    private List<KanbanColumn> kanbanColumns;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
