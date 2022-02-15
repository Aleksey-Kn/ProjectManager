package ru.manager.ProgectManager.entitys;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class KanbanElementComment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String text;

//    @Column
//    private long dateTime;

    @ManyToOne
    @JoinColumn(name = "element_id")
    private KanbanElement kanbanElement;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;
}
