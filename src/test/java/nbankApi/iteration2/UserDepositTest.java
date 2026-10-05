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

public class UserDepositTest {

    private static final String VALID_USER_PASSWORD = "Password123#";
    private static final double BASE_DEPOSIT_AMOUNT = 100.0;
    private static final double MAX_DEPOSIT_LIMIT = 5000.00;
    private static final double MAX_DEPOSIT_LIMIT_MINUS_STEP = 4999.99;
    private static final double MIN_DEPOSIT_LIMIT = 0.01;
    private static final Integer NON_EXISTENT_USER_ID = Integer.MAX_VALUE;

    private static final double MAX_DEPOSIT_LIMIT_PLUS_STEP = 5000.01;
    private static final double ZERO_AMOUNT = 0.00;
    private static final double NEGATIVE_AMOUNT = -0.01;

    private static String adminToken;
    private static String userAToken;
    private static double userAInitialBalance;
    private static Integer userAId;
    private static Integer userAAccountNumber;
    private static String userABankAccountNumber;

    private static String userBToken;
    private static Integer userBId;
    private static Integer userBAccountNumber;

    @BeforeAll
    public static void setUpAll() {
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(DOUBLE));
    }

    @BeforeEach
    public void setUp() {
        String userAName = "userNameA" + UUID.randomUUID().toString().substring(0, 4);
        String userBName = "userNameB" + UUID.randomUUID().toString().substring(0, 4);

        System.out.println("-------Setup: Get admin auth token-------");
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
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", notNullValue())
                .extract()
                .header("Authorization");

        System.out.println("-------Setup: Create user A by admin-------");
        Response createUserAByAdminResponse = given()
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
                        """.formatted(userAName, VALID_USER_PASSWORD))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Authorization", notNullValue())
                .body("id", notNullValue())
                .extract()
                .response();

        userAToken = createUserAByAdminResponse.header("Authorization");
        userAId = createUserAByAdminResponse.jsonPath().getInt("id");

        System.out.println("-------Setup: User A creates account-------");
        Response createAccountByUserAResponse = given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("accountNumber", notNullValue())
                .body("balance", equalTo(0.0))
                .extract()
                .response();
        userAAccountNumber = createAccountByUserAResponse.jsonPath().getInt("id");
        userAInitialBalance = createAccountByUserAResponse.jsonPath().getDouble("balance");

        System.out.println("-------Setup: Create user B by admin-------");
        Response createUserBByAdminResponse = given()
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
                        """.formatted(userBName, VALID_USER_PASSWORD))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .log().status().log().body()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Authorization", notNullValue())
                .body("id", notNullValue())
                .extract()
                .response();

        userBToken = createUserBByAdminResponse.header("Authorization");
        userBId = createUserBByAdminResponse.jsonPath().getInt("id");

        System.out.println("-------Setup: User B creates account-------");
        Response createAccountByUserBResponse = given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .log().status().log().body()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", notNullValue())
                .body("accountNumber", notNullValue())
                .body("balance", equalTo(0.0))
                .extract()
                .response();

        userBAccountNumber = createAccountByUserBResponse.jsonPath().getInt("id");
    }

    @AfterEach
    public void tearDown() {
        if (adminToken != null) {
            if (userAId != null) {
                System.out.println("\n-------User: " + userAId + " deleted-------");
                given().header("Authorization", adminToken)
                        .delete("http://localhost:4111/api/v1/admin/users/" + userAId);
            }
            if (userBId != null) {
                System.out.println("\n-------User: " + userBId + " deleted-------");
                given().header("Authorization", adminToken)
                        .delete("http://localhost:4111/api/v1/admin/users/" + userBId);
            }
        }
    }

    private void assertUserACurrentBalance(double expectedBalance) {
        System.out.println("\n-------Check user balance-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d }.balance" .formatted(userAAccountNumber), equalTo(expectedBalance));
    }

    private void assertDepositTransactionExists(double depositAmount) {
        System.out.println("\n-------Check history transaction (DEPOSIT)-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .pathParam("accountId", userAAccountNumber)
                .get("http://localhost:4111/api/v1/accounts/{accountId}/transactions")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("isEmpty()", is(false))
                .body("find { it.type == 'DEPOSIT' && it.amount == %s }.relatedAccountId".formatted(depositAmount), equalTo(userAAccountNumber));
    }

    @Test
    public void shouldDepositToAccountSuccessfullyWhenValidAmount() {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "id": %d,
                          "balance": %s
                        }
                        """
                        .formatted(userAAccountNumber, BASE_DEPOSIT_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("id", notNullValue())
                .body("accountNumber", equalTo(userABankAccountNumber))
                .body("balance", equalTo(userAInitialBalance + BASE_DEPOSIT_AMOUNT))
                .body("transactions", not(empty()))
                .body("transactions.find { it.type == 'DEPOSIT' }.amount", equalTo(BASE_DEPOSIT_AMOUNT))
                .body("transactions.find { it.type == 'DEPOSIT' }.relatedAccountId", equalTo(userAAccountNumber));

        assertUserACurrentBalance(userAInitialBalance + BASE_DEPOSIT_AMOUNT);
        assertDepositTransactionExists(BASE_DEPOSIT_AMOUNT);
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            MAX_DEPOSIT_LIMIT,
            MAX_DEPOSIT_LIMIT_MINUS_STEP,
            MIN_DEPOSIT_LIMIT
    })
    public void shouldDepositToAccountSuccessfullyWhenValidBoundaryValues(double amount) {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "id": %d,
                          "balance": %s
                        }
                        """
                        .formatted(userAAccountNumber, amount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("id", notNullValue())
                .body("accountNumber", equalTo(userABankAccountNumber))
                .body("balance", equalTo(userAInitialBalance + amount))
                .body("transactions", not(empty()))
                .body("transactions.find { it.type == 'DEPOSIT' }.amount", equalTo(amount))
                .body("transactions.find { it.type == 'DEPOSIT' }.relatedAccountId", equalTo(userAAccountNumber));

        assertUserACurrentBalance(userAInitialBalance + amount);
        assertDepositTransactionExists(amount);
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            MAX_DEPOSIT_LIMIT_PLUS_STEP,
            ZERO_AMOUNT,
            NEGATIVE_AMOUNT,
    })
    public void shouldReturn400WhenAmountInvalidBoundaryValues(double amount) {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "id": %d,
                          "balance": %s
                        }
                        """
                        .formatted(userAAccountNumber, amount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        assertUserACurrentBalance(userAInitialBalance);
    }

    @Test
    public void shouldReturn403WhenDepositToAnotherUserId() {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "id": %d,
                          "balance": %s
                        }
                        """
                        .formatted(userBAccountNumber, BASE_DEPOSIT_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        assertUserACurrentBalance(userAInitialBalance);
    }

    @Test
    public void shouldReturn403WhenDepositToNonExistentUserId() {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "id": %d,
                          "balance": %s
                        }
                        """
                        .formatted(NON_EXISTENT_USER_ID, BASE_DEPOSIT_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        assertUserACurrentBalance(userAInitialBalance);
    }

    @Test
    public void shouldReturn401WhenUnauthorizedUser() {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "id": %d,
                          "balance": %s
                        }
                        """
                        .formatted(userAAccountNumber, BASE_DEPOSIT_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        assertUserACurrentBalance(userAInitialBalance);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "\"500\"",
            "\"five\""
    })
    public void shouldReturn400WhenInvalidDataTypeInAmountField(String amount) {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "id": %d,
                          "balance": %s
                        }
                        """
                        .formatted(userAAccountNumber, amount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        assertUserACurrentBalance(userAInitialBalance);
    }

    @Test
    public void shouldReturn400WithoutBalanceFieldInBody() {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "id": %d
                        }
                        """
                        .formatted(userAAccountNumber))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        assertUserACurrentBalance(userAInitialBalance);
    }

    @Test
    public void shouldReturn400WithoutIdFieldInBody() {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                         "balance": %s
                        }
                        """
                        .formatted(BASE_DEPOSIT_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        assertUserACurrentBalance(userAInitialBalance);
    }

    @Test
    public void shouldReturn400WhenRequestWithoutBody() {
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("{}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        assertUserACurrentBalance(userAInitialBalance);
    }
}