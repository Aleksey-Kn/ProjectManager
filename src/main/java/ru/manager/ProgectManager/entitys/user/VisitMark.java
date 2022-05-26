package ru.manager.ProgectManager.entitys.user;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.enums.ResourceType;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
public class VisitMark {
    @Id
    @GeneratedValue
    private long id;

    @Enumerated
    private ResourceType resourceType;

    @Column(nullable = false)
    private String resourceName;
    private int serialNumber;
    private long resourceId;
    private String description;
    private long projectId;
    private String projectName;

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
