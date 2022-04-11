package ru.manager.ProgectManager.entitys.kanban;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "kanban_column")
public class KanbanColumn {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private int serialNumber;

    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @Column(nullable = false)
    private int delayedDays; // 0 - значение, обоначающее отстутствие автоочищения столбца

    @JsonIgnore
    @OneToMany(mappedBy = "kanbanColumn", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<KanbanElement> elements;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "kanban_id")
    private Kanban kanban;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        KanbanColumn that = (KanbanColumn) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
