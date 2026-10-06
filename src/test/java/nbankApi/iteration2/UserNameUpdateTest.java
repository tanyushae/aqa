package nbankApi.iteration2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.DOUBLE;
import static org.hamcrest.Matchers.*;

public class UserNameUpdateTest {

    private static final String VALID_DEFAULT_PROFILE_NAME = "Default Name";
    private static final String VALID_USER_PASSWORD = "Password123#";
    private static final String VALID_PROFILE_NAME = "New Name";
    private static final String VALID_PROFILE_RENAME = "New ReName";
    private static final String VALID_PROFILE_CYRILLIC_NAME = "Иван Иванов";
    private static final String INVALID_PROFILE_NAME_ONE_WORD_ONLY = "NewName";
    private static final String INVALID_PROFILE_NAME_THREE_WORDS = "New Name Name";
    private static final String INVALID_PROFILE_NAME_AS_EMPTY_STRING = "";
    private static final String INVALID_PROFILE_NAME_WITH_DIGITS = "Lena2 Rect";
    private static final String INVALID_PROFILE_NAME_WITH_CHARACTER = "Ivan-Petrov";
    public static final String INVALID_USER_TOKEN = "InvalidToken123";

    private String adminToken;
    private String userToken;
    private String userName;
    private Integer userId;

    @BeforeAll
    public static void setUpAll() {
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(DOUBLE));
    }

    @BeforeEach
    public void setUp() {
        System.out.println("-------Setup: userName generation------");
        userName = "userName" + UUID.randomUUID().toString().substring(0, 4);

        System.out.println("\n-------Setup: Get admin auth token-------");
        adminToken = given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                         "username": "admin",
                         "password": "admin"
                        }
                 """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", notNullValue())
                .extract()
                .header("Authorization");

        System.out.println("\n-------Setup: Create user by admin-------");
        Response createUserByAdminResponse = given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", adminToken)
                .body("""
                           {
                           "username": "%s",
                           "password": "%s",
                           "role": "USER"
                           }
                        """.formatted(userName, VALID_USER_PASSWORD))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Authorization", notNullValue())
                .body("id", notNullValue())
                .extract()
                .response();

        userToken = createUserByAdminResponse.header("Authorization");
        userId = createUserByAdminResponse.jsonPath().getInt("id");

        System.out.println("\n-------Setup: Set default valid profile name-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .body("""
                        {
                         "name": "%s"
                        }
                        """.formatted(VALID_DEFAULT_PROFILE_NAME))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Profile updated successfully"))
                .body("customer.id", equalTo(userId))
                .body("customer.username", equalTo(userName))
                .body("customer.name", equalTo(VALID_DEFAULT_PROFILE_NAME));

    }

    @AfterEach
    public void tearDown() {
        if (adminToken != null && userId != null) {
            System.out.println("\n-------User: " + userId + " deleted-------");
            given().header("Authorization", adminToken)
                    .delete("http://localhost:4111/api/v1/admin/users/" + userId);
        }
    }

    @ParameterizedTest(name = "Case #{index}: successful update to valid name: \"{0}\"")
    @ValueSource(strings = {
            VALID_PROFILE_NAME,
            VALID_PROFILE_RENAME,
            VALID_PROFILE_CYRILLIC_NAME
    })
    public void shouldSuccessfullyUpdateValidProfileName(String newProfileName) {
        System.out.println("\n-------Update profile name to: " + newProfileName + "-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .body("""
                           {
                           "name": "%s"
                           }
                        """.formatted(newProfileName))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Profile updated successfully"))
                .body("customer.id", equalTo(userId))
                .body("customer.username", equalTo(userName))
                .body("customer.name", equalTo(newProfileName));

        System.out.println("\n-------Check: profile name updated-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(userId))
                .body("name", equalTo(newProfileName));
    }

    @ParameterizedTest(name = "Case #{index}: fail to update profile with invalid name: \"{0}\"")
    @NullSource
    @ValueSource(strings = {
            INVALID_PROFILE_NAME_ONE_WORD_ONLY,
            INVALID_PROFILE_NAME_THREE_WORDS,
            INVALID_PROFILE_NAME_AS_EMPTY_STRING,
            INVALID_PROFILE_NAME_WITH_DIGITS,
            INVALID_PROFILE_NAME_WITH_CHARACTER
    })
    public void shouldReturn400WhenInvalidProfileName(String invalidProfileName) {
        System.out.println("\n-------Update profile name to: " + invalidProfileName + "-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .body("""
                           {
                           "name": "%s"
                           }
                        """.formatted(invalidProfileName))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: profile name did not update-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(userId))
                .body("name", equalTo(VALID_DEFAULT_PROFILE_NAME));
    }

    @Test
    public void shouldReturn403WhenUnauthorizedUser() {
        System.out.println("\n-------Unauthorized user cannot update profile name-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", INVALID_USER_TOKEN)
                .body("""
                           {
                           "name": "%s"
                           }
                        """.formatted(VALID_PROFILE_NAME))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        System.out.println("\n-------Check: profile name did not update-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(userId))
                .body("name", equalTo(VALID_DEFAULT_PROFILE_NAME));
    }

    @Test
    public void shouldReturn400WhenRequestWithoutNameFieldInBody() {
        System.out.println("\n-------Update profile name request without name field-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .body("{}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: profile name did not update-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userToken)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(userId))
                .body("name", equalTo(VALID_DEFAULT_PROFILE_NAME));
    }
}