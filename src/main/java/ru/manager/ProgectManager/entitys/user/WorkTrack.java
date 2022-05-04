package ru.manager.ProgectManager.entitys.user;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
public class WorkTrack {
    @Id
    @GeneratedValue
    private long id;

    private int workTime;
    private long workDate;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private KanbanElement task;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkTrack workTrack = (WorkTrack) o;
        return id == workTrack.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
