package nbankApi.iteration2.middle.tests;

import io.restassured.RestAssured;
import nbankApi.iteration2.middle.requests.admin.AdminDeleteUserRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;
import nbankApi.iteration2.middle.support.steps.SetupSteps;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.DOUBLE;

public class BaseTest {
    protected static String adminToken;
    protected SoftAssertions softly;
    private static final ThreadLocal<List<Integer>> CREATED_USERS = ThreadLocal.withInitial(ArrayList::new);

    @BeforeAll
    public static void setUpAll() {
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(DOUBLE));
        adminToken = SetupSteps.getAdminToken();
    }

    public static void trackUser(int userId) {
        CREATED_USERS.get().add(userId);
    }

    @BeforeEach
    public void setupSoftAssertions() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void tearDown() {
        try {
            if (softly != null) {
                softly.assertAll();
            }
        } finally {
            List<Integer> userIds = CREATED_USERS.get();
            for (int userId : userIds) {
                try {
                    new AdminDeleteUserRequest(
                            RequestSpecs.authSpec(adminToken),
                            ResponseSpecs.successOk(),
                            userId
                    ).delete();
                    System.out.println("Clean-up: Successfully deleted user with ID: " + userId);
                } catch (Exception e) {
                    System.err.println("Clean-up warning: Could not delete user with ID " + userId);
                }
            }
            userIds.clear();
        }
    }
}