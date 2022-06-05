package ru.manager.ProgectManager.entitys.accessProject;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import ru.manager.ProgectManager.entitys.kanban.Kanban;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
public class CustomRoleWithKanbanConnector {
    @Id
    @GeneratedValue
    private long id;

    private boolean canEdit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private CustomProjectRole customProjectRole;

    @ManyToOne(optional = false)
    @JoinColumn(name = "kanban_id")
    private Kanban kanban;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CustomRoleWithKanbanConnector that = (CustomRoleWithKanbanConnector) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
