package ru.manager.ProgectManager.entitys.kanban;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.entitys.user.WorkTrack;
import ru.manager.ProgectManager.enums.ElementStatus;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "kanban_element")
@Getter
@Setter
public class KanbanElement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "last_redactor_id")
    private User lastRedactor;

    @Column
    private long timeOfCreate;

    @Column
    private long timeOfUpdate;

    @Column(nullable = false)
    private int serialNumber;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String content;

    @Enumerated(EnumType.ORDINAL)
    private ElementStatus status;

    @Column
    private long selectedDate;

    @ManyToOne
    @JoinColumn(name = "kanban_column_id")
    private KanbanColumn kanbanColumn;

    @OneToMany(mappedBy = "element", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<KanbanAttachment> kanbanAttachments;

    @OneToMany(mappedBy = "kanbanElement", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<KanbanElementComment> comments;

    @OneToMany(mappedBy = "element", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CheckBox> checkBoxes;

    @OneToMany(mappedBy = "task")
    private Set<WorkTrack> workTrackSet;

    @ManyToMany(cascade = {
            CascadeType.REFRESH,
            CascadeType.MERGE
    })
    @JoinTable(name = "kanban_element_with_tag_connector",
            joinColumns = {@JoinColumn(name = "kanban_element_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")}
    )
    private Set<Tag> tags;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KanbanElement element = (KanbanElement) o;
        return id == element.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
