package ru.manager.ProgectManager.entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kanban kanban = (Kanban) o;
        return id == kanban.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
