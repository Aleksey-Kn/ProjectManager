package ru.manager.ProgectManager.base;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.manager.ProgectManager.init.MySqlInitializer;
import ru.manager.ProgectManager.repositories.ApproveActionTokenRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.services.user.UserService;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(initializers = MySqlInitializer.class)
public abstract class ProjectManagerTestBase {
    @Autowired
    protected UserService userService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @AfterEach
    void removeUser(@Autowired ApproveActionTokenRepository approveActionTokenRepository) {
        approveActionTokenRepository.deleteAll();
        userRepository.deleteAll();
    }
}
