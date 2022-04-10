package ru.manager.ProgectManager.entitys.kanban;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
public class CheckBox {
    @Id
    @GeneratedValue
    private long id;

    private boolean check;
    private String text;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "element_id")
    private KanbanElement element;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CheckBox checkBox = (CheckBox) o;
        return Objects.equals(id, checkBox.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
