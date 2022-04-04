package ru.manager.ProgectManager.entitys.accessProject;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.enums.TypeRoleProject;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
public class AccessProject {
    @Id
    private String code;

    @Enumerated
    private TypeRoleProject typeRoleProject;

    @ManyToOne
    @JoinColumn(name = "project_role_id")
    private CustomProjectRole projectRole;

    @Column
    private boolean disposable;

    @Column
    private long timeForDie; // in days

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AccessProject that = (AccessProject) o;
        return code != null && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
