package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    void deleteAllByLogin(String login);
}
