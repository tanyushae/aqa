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

public class TransferFromOneAccountToAnotherTest {

    private static final String VALID_USER_PASSWORD = "Password123#";
    private static final double SETUP_DEPOSIT_AMOUNT = 5000.00;
    private static final double BASE_TRANSFER_AMOUNT = 100.00;
    private static final double INITIAL_SENDER_BALANCE = SETUP_DEPOSIT_AMOUNT * 3;
    private static final Integer NON_EXISTENT_USER_ID = Integer.MAX_VALUE;
    public static final String INVALID_USER_TOKEN = "InvalidToken123";

    private static final double MAX_TRANSFER_LIMIT = 10000.00;
    private static final double MAX_TRANSFER_LIMIT_MINUS_STEP = 9999.99;
    private static final double MIN_TRANSFER_LIMIT = 0.01;

    private static final double MAX_TRANSFER_LIMIT_PLUS_STEP = 10000.01;
    private static final double ZERO_AMOUNT = 0.00;
    private static final double NEGATIVE_AMOUNT = -0.01;

    private String adminToken;

    private String userAToken;
    private Integer userAId;
    private Integer userAAccountNumber1;
    private Integer userAAccountNumber2;
    private String userABankAccountNumber1;
    private String userABankAccountNumber2;
    private double userAInitialBalanceBankAccountNumber1;
    private double userAInitialBalanceBankAccountNumber2;
    private double userACurrentBalanceBankAccountNumber2;

    private Integer userBId;
    private String userBToken;
    private Integer userBAccountNumber;
    private String userBBankAccountNumber;
    private double userBInitialBalanceBankAccount;

    @BeforeAll
    public static void setUpAll() {
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(DOUBLE));
    }

    @BeforeEach
    public void setUp() {
        System.out.println("-------Setup: userNameA and userNameB------");
        String userAName = "userNameA" + UUID.randomUUID().toString().substring(0, 4);
        String userBName = "userNameB" + UUID.randomUUID().toString().substring(0, 4);

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

        System.out.println("\n-------Setup: Create user A by admin-------");
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

        System.out.println("\n-------Setup: User A creates account 1-------");
        Response createAccountA1ByUserAResponse = given()
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
        userAAccountNumber1 = createAccountA1ByUserAResponse.jsonPath().getInt("id");
        userABankAccountNumber1 = createAccountA1ByUserAResponse.jsonPath().getString("accountNumber");
        userAInitialBalanceBankAccountNumber1 = createAccountA1ByUserAResponse.jsonPath().getDouble("balance");

        System.out.println("\n-------Setup: User A creates account 2-------");
        Response createAccountA2ByUserAResponse = given()
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
        userAAccountNumber2 = createAccountA2ByUserAResponse.jsonPath().getInt("id");
        userABankAccountNumber2 = createAccountA2ByUserAResponse.jsonPath().getString("accountNumber");
        userAInitialBalanceBankAccountNumber2 = createAccountA2ByUserAResponse.jsonPath().getDouble("balance");

        System.out.println("\n-------Setup: Create user B by admin-------");
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
                .statusCode(HttpStatus.SC_CREATED)
                .header("Authorization", notNullValue())
                .body("id", notNullValue())
                .extract()
                .response();
        userBToken = createUserBByAdminResponse.header("Authorization");
        userBId = createUserBByAdminResponse.jsonPath().getInt("id");

        System.out.println("\n-------Setup: User B creates account 1-------");
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
        userBBankAccountNumber = createAccountByUserBResponse.jsonPath().getString("accountNumber");
        userBInitialBalanceBankAccount = createAccountByUserBResponse.jsonPath().getDouble("balance");

        System.out.println("\n-------Setup: 3 deposits to user-------");
        for (int i = 0; i < 3; i++) {
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
                            .formatted(userAAccountNumber1, SETUP_DEPOSIT_AMOUNT))
                    .post("http://localhost:4111/api/v1/accounts/deposit")
                    .then()
                    .log().status().log().body()
                    .statusCode(HttpStatus.SC_OK);
        }
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

    @Test
    public void shouldTransferToAnotherUserSuccessfullyWhenAllDataValid() {
        System.out.println("\n-------Base transfer to another user-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber1, userBAccountNumber, BASE_TRANSFER_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Transfer successful"))
                .body("senderAccountId", equalTo(userAAccountNumber1))
                .body("amount", equalTo(BASE_TRANSFER_AMOUNT))
                .body("receiverAccountId", equalTo(userBAccountNumber));

        System.out.println("\n-------Verification of decrease in the user's A balance-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE - BASE_TRANSFER_AMOUNT));

        System.out.println("\n-------Verification of increase in the user's B balance-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount + BASE_TRANSFER_AMOUNT));

        System.out.println("\n-------Check history sender transaction (TRANSFER_OUT)-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .pathParam("accountId", userAAccountNumber1)
                .get("http://localhost:4111/api/v1/accounts/{accountId}/transactions")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.type == 'TRANSFER_OUT' }.id", notNullValue())
                .body("find { it.type == 'TRANSFER_OUT' }.amount", equalTo(BASE_TRANSFER_AMOUNT))
                .body("find { it.type == 'TRANSFER_OUT' }.relatedAccountId", equalTo(userBAccountNumber));

        System.out.println("\n-------Check history receiver transaction (TRANSFER_IN)-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .pathParam("accountId", userBAccountNumber)
                .get("http://localhost:4111/api/v1/accounts/{accountId}/transactions")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.type == 'TRANSFER_IN' }.id", notNullValue())
                .body("find { it.type == 'TRANSFER_IN' }.amount", equalTo(BASE_TRANSFER_AMOUNT))
                .body("find { it.type == 'TRANSFER_IN' }.relatedAccountId", equalTo(userAAccountNumber1));
    }

    @Test
    public void shouldTransferBetweenOwnAccountsSuccessfullyWhenAllDateValid() {
        System.out.println("\n-------Base transfer between owner accounts-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber1, userAAccountNumber2, BASE_TRANSFER_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Transfer successful"))
                .body("senderAccountId", equalTo(userAAccountNumber1))
                .body("amount", equalTo(BASE_TRANSFER_AMOUNT))
                .body("receiverAccountId", equalTo(userAAccountNumber2));

        System.out.println("\n-------Verification of decrease in the user's A balance account 1-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE - BASE_TRANSFER_AMOUNT));

        System.out.println("\n-------Verification of increase in the user's A balance account 2-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber2, userABankAccountNumber2),
                        equalTo(userAInitialBalanceBankAccountNumber2 + BASE_TRANSFER_AMOUNT));

        System.out.println("\n-------Check history sender transaction (TRANSFER_OUT)-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .pathParam("accountId", userAAccountNumber1)
                .get("http://localhost:4111/api/v1/accounts/{accountId}/transactions")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.type == 'TRANSFER_OUT' }.id", notNullValue())
                .body("find { it.type == 'TRANSFER_OUT' }.amount", equalTo(BASE_TRANSFER_AMOUNT))
                .body("find { it.type == 'TRANSFER_OUT' }.relatedAccountId", equalTo(userAAccountNumber2));

        System.out.println("\n-------Check history receiver transaction (TRANSFER_IN)-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .pathParam("accountId", userAAccountNumber2)
                .get("http://localhost:4111/api/v1/accounts/{accountId}/transactions")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.type == 'TRANSFER_IN' }.id", notNullValue())
                .body("find { it.type == 'TRANSFER_IN' }.amount", equalTo(BASE_TRANSFER_AMOUNT))
                .body("find { it.type == 'TRANSFER_IN' }.relatedAccountId", equalTo(userAAccountNumber1));
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            MAX_TRANSFER_LIMIT,
            MAX_TRANSFER_LIMIT_MINUS_STEP,
            MIN_TRANSFER_LIMIT
    })
    public void shouldTransferToAccountSuccessfullyWhenValidBoundaryValues(double amount) {
        System.out.println("\n-------Transfer with valid boundary values-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber1, userBAccountNumber, amount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Transfer successful"))
                .body("senderAccountId", equalTo(userAAccountNumber1))
                .body("amount", equalTo(amount))
                .body("receiverAccountId", equalTo(userBAccountNumber));

        System.out.println("\n-------Verification of decrease in the user's A balance-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE - amount));

        System.out.println("\n-------Verification of increase in the user's B balance-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount + amount));

        System.out.println("\n-------Check sender transaction in history-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .pathParam("accountId", userAAccountNumber1)
                .get("http://localhost:4111/api/v1/accounts/{accountId}/transactions")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.type == 'TRANSFER_OUT' }.id", notNullValue())
                .body("find { it.type == 'TRANSFER_OUT' }.amount", equalTo(amount))
                .body("find { it.type == 'TRANSFER_OUT' }.relatedAccountId", equalTo(userBAccountNumber));

        System.out.println("\n-------Check receiver transaction in history (TRANSFER_IN)-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .pathParam("accountId", userBAccountNumber)
                .get("http://localhost:4111/api/v1/accounts/{accountId}/transactions")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.type == 'TRANSFER_IN' }.id", notNullValue())
                .body("find { it.type == 'TRANSFER_IN' }.amount", equalTo(amount))
                .body("find { it.type == 'TRANSFER_IN' }.relatedAccountId", equalTo(userAAccountNumber1));
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            MAX_TRANSFER_LIMIT_PLUS_STEP,
            ZERO_AMOUNT,
            NEGATIVE_AMOUNT
    })
    public void shouldReturn400TransferWithInvalidBoundaryValues(double amount) {
        System.out.println("\n-------Transfer with invalid boundary values-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber1, userBAccountNumber, amount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount));
    }

    @Test
    public void shouldReturn400WhenTransferAmountExceedingCurrentBalance() {
        System.out.println("\n-------Setup: Deposit to user A account 2-------");
        userACurrentBalanceBankAccountNumber2 = given()
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
                        .formatted(userAAccountNumber2, SETUP_DEPOSIT_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("id", notNullValue())
                .body("accountNumber", equalTo(userABankAccountNumber2))
                .body("balance", equalTo(SETUP_DEPOSIT_AMOUNT))
                .extract()
                .jsonPath().getDouble("balance");

        System.out.println("\n-------Transfer with exceeding amount-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber2, userBAccountNumber, userACurrentBalanceBankAccountNumber2 + 10.00))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber2, userABankAccountNumber2),
                        equalTo(SETUP_DEPOSIT_AMOUNT));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "\"5\"",
            "\"five thousand\""
    })
    public void shouldReturn400ForInvalidDataTypeInAmountField(String amount) {
        System.out.println("\n-------Transfer with invalid data type in amount field-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber1, userBAccountNumber, amount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount));
    }

    @Test
    public void shouldReturn403WhenTransferFromAnotherUserAccount() {
        System.out.println("\n-------Setup: Deposit to sender account-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .body("""
                        {
                          "id": %d,
                          "balance": %s
                        }
                        """
                        .formatted(userBAccountNumber, SETUP_DEPOSIT_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("id", notNullValue())
                .body("accountNumber", equalTo(userBBankAccountNumber))
                .body("balance", equalTo(SETUP_DEPOSIT_AMOUNT));

        System.out.println("\n-------Transfer money from another user's account-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userBAccountNumber, userAAccountNumber2, BASE_TRANSFER_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(SETUP_DEPOSIT_AMOUNT));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber2, userABankAccountNumber2),
                        equalTo(userAInitialBalanceBankAccountNumber2));
    }

    @Test
    public void shouldReturn403WhenSenderAccountIdIsNotExist() {
        System.out.println("\n-------Transfer money from non-existent account-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(NON_EXISTENT_USER_ID, userBAccountNumber, BASE_TRANSFER_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        System.out.println("\n-------Check: sender's balance remained unchanged (BankAccountNumber1)-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));

        System.out.println("\n-------Check: sender's balance remained unchanged (BankAccountNumber2)-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber2, userABankAccountNumber2),
                        equalTo(userAInitialBalanceBankAccountNumber2));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount));
    }

    @Test
    public void shouldReturn400WhenReceiverAccountIdIsNotExist() {
        System.out.println("\n-------Transfer money to non-existent account-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber1, NON_EXISTENT_USER_ID, BASE_TRANSFER_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));
    }

    @Test
    public void shouldReturn401WhenSenderUnauthorized() {
        System.out.println("\n-------Transfer money from unauthorized user-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", INVALID_USER_TOKEN)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber1, userBAccountNumber, BASE_TRANSFER_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount));
    }

    @Test
    public void shouldReturn400WhenSenderIdAndReceiverIdTheSame() {
        System.out.println("\n-------Transfer with the same sender id and receiver id-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber1, userAAccountNumber1, BASE_TRANSFER_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));
    }

    @Test
    public void shouldReturn400WhenRequestWithoutAmountFieldInBody() {
        System.out.println("\n-------Transfer without amount field in body-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d
                        }
                        """
                        .formatted(userAAccountNumber1, userBAccountNumber))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount));
    }

    @Test
    public void shouldReturn400WhenRequestWithoutSenderAccountIdFieldInBody() {
        System.out.println("\n-------Transfer without senderAccountId field in body-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                          "receiverAccountId": %d,
                           "amount": %s
                        }
                        """
                        .formatted(userBAccountNumber, BASE_TRANSFER_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount));
    }

    @Test
    public void shouldReturn400WhenRequestWithoutReceiverAccountIdFieldInBody() {
        System.out.println("\n-------Transfer without receiverAccountId field in body-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("""
                        {
                           "senderAccountId": %d,
                           "amount": %s
                        }
                        """
                        .formatted(userAAccountNumber1, BASE_TRANSFER_AMOUNT))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount));
    }

    @Test
    public void shouldReturn400WhenRequestWithoutBody() {
        System.out.println("\n-------Transfer without body-------");
        given()
                .log().uri().log().headers().log().body()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .body("{}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        System.out.println("\n-------Check: sender's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userAToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userAAccountNumber1, userABankAccountNumber1),
                        equalTo(INITIAL_SENDER_BALANCE));

        System.out.println("\n-------Check: receiver's balance remained unchanged-------");
        given()
                .log().uri().log().headers()
                .accept(ContentType.JSON)
                .header("Authorization", userBToken)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .log().status().log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == %d && it.accountNumber == '%s' }.balance"
                                .formatted(userBAccountNumber, userBBankAccountNumber),
                        equalTo(userBInitialBalanceBankAccount));
    }
}