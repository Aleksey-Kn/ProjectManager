package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.user.ApproveActionToken;

public interface ApproveActionTokenRepository extends CrudRepository<ApproveActionToken, String> {
}
