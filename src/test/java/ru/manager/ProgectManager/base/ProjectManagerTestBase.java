package ru.manager.ProgectManager.base;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.manager.ProgectManager.components.LocalisedMessages;
import ru.manager.ProgectManager.init.MySqlInitializer;
import ru.manager.ProgectManager.repositories.ApproveActionTokenRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.services.user.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    protected LocalisedMessages localisedMessages;

    @RegisterExtension
    protected static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication())
            .withPerMethodLifecycle(true);

    @AfterEach
    void removeUser(@Autowired ApproveActionTokenRepository approveActionTokenRepository) {
        approveActionTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected void assertInTransaction(Runnable check) {
        transactionTemplate.execute(ts -> {
            check.run();
            return null;
        });
    }
}
