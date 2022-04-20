package ru.manager.ProgectManager.entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.enums.ResourceType;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
public class VisitMark {
    @Id
    @GeneratedValue
    @JsonIgnore
    private long id;

    @Enumerated
    private ResourceType resourceType;

    private long resourceId;
    private byte serialNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisitMark visitMark = (VisitMark) o;
        return id == visitMark.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
