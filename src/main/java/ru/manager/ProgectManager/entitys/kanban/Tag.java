package ru.manager.ProgectManager.entitys.kanban;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
public class Tag {
    @Id
    @GeneratedValue
    private long id;

    private String text;
    private String color;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "kanban_id")
    private Kanban kanban;

    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    private Set<KanbanElement> kanbanElementSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id == tag.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
