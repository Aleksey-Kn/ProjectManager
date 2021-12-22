package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.User;

public interface UserRepository extends CrudRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
}
