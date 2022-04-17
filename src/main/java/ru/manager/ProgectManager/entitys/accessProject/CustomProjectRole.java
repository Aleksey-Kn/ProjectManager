package ru.manager.ProgectManager.entitys.accessProject;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
public class CustomProjectRole {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String name;

    @Column
    private boolean canEditResources;

    @OneToMany(cascade = CascadeType.REMOVE)
    private Set<CustomRoleWithKanbanConnector> customRoleWithKanbanConnectors;

    @OneToMany(cascade = CascadeType.REMOVE)
    private Set<CustomRoleWithDocumentConnector> customRoleWithDocumentConnectors;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CustomProjectRole that = (CustomProjectRole) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
