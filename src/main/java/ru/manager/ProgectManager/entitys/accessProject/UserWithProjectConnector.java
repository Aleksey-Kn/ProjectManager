package ru.manager.ProgectManager.entitys.accessProject;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
public class UserWithProjectConnector {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Enumerated
    private TypeRoleProject roleType;

    @ManyToOne
    @JoinColumn(name = "project_role_id")
    private CustomProjectRole customProjectRole;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserWithProjectConnector connector = (UserWithProjectConnector) o;
        return id == connector.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
