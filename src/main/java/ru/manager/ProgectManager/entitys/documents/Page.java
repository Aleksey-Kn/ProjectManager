package ru.manager.ProgectManager.entitys.documents;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.Project;

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

    private String content;

    @ManyToOne
    @JoinColumn(name = "root_id")
    private Page root;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Page parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private Set<Page> subpages;

    @ManyToOne
    @JoinColumn(name = "kanban_id")
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
