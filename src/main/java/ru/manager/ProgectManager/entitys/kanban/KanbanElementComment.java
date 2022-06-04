package ru.manager.ProgectManager.entitys.kanban;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.user.User;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
public class KanbanElementComment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String text;

    @Column
    private long dateTime;

    @Column
    private boolean redacted;

    @ManyToOne
    @JoinColumn(name = "element_id")
    private KanbanElement kanbanElement;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KanbanElementComment comment = (KanbanElementComment) o;
        return id == comment.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
