package ru.manager.ProgectManager.init;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class GreenMailInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final GenericContainer<?> greenMailContainer =
            new GenericContainer<>(DockerImageName.parse("greenmail/standalone:1.6.1"));

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        greenMailContainer.waitingFor(Wait.forLogMessage(".*Starting GreenMail standalone.*", 1))
                .withEnv("GREENMAIL_OPTS",
                        "-Dgreenmail.setup.test.smtp -Dgreenmail.hostname=0.0.0.0 -Dgreenmail.auth.disabled")
                .withExposedPorts(3025)
                .start();
        TestPropertyValues.of(
                "spring.mail.host=" + greenMailContainer.getHost(),
                "spring.mail.port=" + greenMailContainer.getFirstMappedPort()
        ).applyTo(applicationContext.getEnvironment());
    }
}
