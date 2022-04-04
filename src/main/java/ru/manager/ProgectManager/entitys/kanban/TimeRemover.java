package ru.manager.ProgectManager.entitys.kanban;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
public class TimeRemover {
    @Id
    private long removerId;

    @Column(nullable = false)
    private long timeToDelete;

    @Column(nullable = false)
    private boolean hard;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeRemover that = (TimeRemover) o;
        return removerId == that.removerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(removerId);
    }
}
