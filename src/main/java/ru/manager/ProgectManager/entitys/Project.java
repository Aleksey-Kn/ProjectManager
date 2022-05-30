package ru.manager.ProgectManager.entitys;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.accessProject.AccessProject;
import ru.manager.ProgectManager.entitys.accessProject.CustomProjectRole;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.entitys.kanban.Kanban;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 76_800)
    @Lob
    private byte[] photo;

    @Column
    private String description;

    @Column
    private String status;

    @Column
    private String statusColor;

    @Column
    private String startDate;

    @Column
    private String deadline;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CustomProjectRole> availableRole;

    @OneToMany(mappedBy = "project", cascade = CascadeType.PERSIST)
    private Set<UserWithProjectConnector> connectors;

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Kanban> kanbans;

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    private Set<Page> pages;

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE)
    private Set<AccessProject> accessOnThis;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id == project.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
