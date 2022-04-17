package ru.manager.ProgectManager.entitys.documents;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.Project;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Section {
    @Id
    @GeneratedValue
    private long id;

    private String name;
    private String content;

    @ManyToOne
    @JoinColumn(name = "kanban_id")
    private Project project;
}
