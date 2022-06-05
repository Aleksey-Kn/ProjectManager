package ru.manager.ProgectManager.entitys.documents;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;
import ru.manager.ProgectManager.entitys.user.User;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
public class Page {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 5_242_880)
    private String content;

    private long updateTime;
    private boolean published;
    private short serialNumber;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "root_id")
    private Page root;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Page parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private Set<Page> subpages;

    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE)
    private Set<CustomRoleWithDocumentConnector> roleConnectors;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return id == page.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
