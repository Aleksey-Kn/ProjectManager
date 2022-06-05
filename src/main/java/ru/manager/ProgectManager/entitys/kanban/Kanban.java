package ru.manager.ProgectManager.entitys.kanban;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;

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

    @Column(length = 76_800)
    @Lob
    private byte[] photo;

    @JsonIgnore
    @OneToMany(mappedBy = "kanban", cascade = CascadeType.REMOVE)
    private Set<Tag> availableTags;

    @JsonIgnore
    @OneToMany(mappedBy = "kanban", cascade = CascadeType.ALL)
    private Set<KanbanColumn> kanbanColumns;

    @JsonIgnore
    @OneToMany(mappedBy = "kanban", cascade = CascadeType.REMOVE)
    private Set<CustomRoleWithKanbanConnector> roleConnectors;

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
