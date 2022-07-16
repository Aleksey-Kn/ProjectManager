package ru.manager.ProgectManager.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.manager.ProgectManager.init.MySqlInitializer;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(initializers = MySqlInitializer.class)
public abstract class ProjectManagerTestBase {
}
