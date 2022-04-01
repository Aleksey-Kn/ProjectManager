package ru.manager.ProgectManager.entitys;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.enums.TypeRoleProject;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class UserWithProjectConnector {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Enumerated
    private TypeRoleProject roleType;

    private boolean canEditResource;

    @OneToMany
    private List<KanbanConnector> kanbanConnectors;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
