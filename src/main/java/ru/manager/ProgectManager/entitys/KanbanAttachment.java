package ru.manager.ProgectManager.entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class KanbanAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(length = 5_242_880)
    @Lob
    private byte[] fileData;

    @Column
    private String filename;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "element_id")
    private KanbanElement element;
}
