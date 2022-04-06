package ru.manager.ProgectManager.entitys.kanban;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.Project;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
public class Kanban {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.REMOVE)
    private Set<Tag> availableTags;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    private Set<KanbanColumn> kanbanColumns;

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
