package ru.manager.ProgectManager.entitys.accessProject;

import lombok.Getter;
import lombok.Setter;
import ru.manager.ProgectManager.entitys.documents.Page;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
public class CustomRoleWithDocumentConnector {
    @Id
    @GeneratedValue
    private long id;

    private boolean canEdit;

    @ManyToOne
    @JoinColumn(name = "root_page_id")
    private Page page;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomRoleWithDocumentConnector that = (CustomRoleWithDocumentConnector) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
