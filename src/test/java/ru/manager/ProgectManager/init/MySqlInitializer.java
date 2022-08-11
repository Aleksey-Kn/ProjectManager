package ru.manager.ProgectManager.init;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MySqlInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final DockerImageName IMAGE = DockerImageName.parse("mysql:8.0.27");
    private static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>(IMAGE);

    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        MY_SQL_CONTAINER
                .withUsername("al")
                .withPassword("aladmin")
                .withDatabaseName("project_manager")
                .withStartupTimeout(Duration.ofMinutes(5))
                .start();
        TestPropertyValues.of(
                "spring.datasource.url=" + MY_SQL_CONTAINER.getJdbcUrl(),
                "spring.datasource.username=" + MY_SQL_CONTAINER.getUsername(),
                "spring.datasource.password=" + MY_SQL_CONTAINER.getPassword(),
                "flyway.user=", MY_SQL_CONTAINER.getUsername(),
                "flyway.password=", MY_SQL_CONTAINER.getPassword()
        ).applyTo(applicationContext.getEnvironment());
    }
}