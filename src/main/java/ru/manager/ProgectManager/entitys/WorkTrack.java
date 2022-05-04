package ru.manager.ProgectManager.entitys;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
public class WorkTrack {
    @Id
    @GeneratedValue
    private long id;

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
