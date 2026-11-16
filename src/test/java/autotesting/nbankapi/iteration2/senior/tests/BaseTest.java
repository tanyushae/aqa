package autotesting.nbankapi.iteration2.senior.tests;

import autotesting.nbankapi.iteration2.senior.steps.AdminSteps;
import autotesting.nbankapi.iteration2.senior.steps.CreatedUsersRegistry;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
public class BaseTest {

    @InjectSoftAssertions
    protected SoftAssertions softly;

    @AfterEach
    public void deleteCreatedUsers() {
        for (int userId : CreatedUsersRegistry.getAndClearUserIds()) {
            try {
                AdminSteps.deleteUser(userId);
            } catch (Exception e) {
                System.err.println("Clean-up warning: Could not delete user with ID " + userId + ". Error: " + e.getMessage());
            }
        }
    }
}