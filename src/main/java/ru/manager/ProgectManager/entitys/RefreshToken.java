package ru.manager.ProgectManager.entitys;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class RefreshToken {
    @Id
    private String token;

    @Column
    private long timeToDie;

    @Column(nullable = false, unique = true)
    private String login;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RefreshToken that = (RefreshToken) o;
        return token != null && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
