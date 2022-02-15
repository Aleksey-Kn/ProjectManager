package ru.manager.ProgectManager.entitys;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

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

    @Column(nullable = false)
    private int serialNumber;

    @Column(nullable = false)
    private String name;

    @Column
    private String tag;

    @Column
    private String content;

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<KanbanAttachment> kanbanAttachments;

    @ManyToOne
    @JoinColumn(name = "kanban_column_id")
    private KanbanColumn kanbanColumn;

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<KanbanElementComment> comments;
}
