package nbankApi.iteration2.middle.tests;

import nbankApi.iteration2.middle.assertions.DepositAssertions;
import nbankApi.iteration2.middle.assertions.TransactionAssertions;
import nbankApi.iteration2.middle.support.steps.SetupSteps;
import nbankApi.iteration2.middle.assertions.AccountAssertions;
import nbankApi.iteration2.middle.models.deposit.DepositRequestDto;
import nbankApi.iteration2.middle.requests.deposit.DepositRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;

import static nbankApi.iteration2.middle.constants.ApiConstants.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

public class UserDepositTest extends BaseTest {

    private static final double BASE_DEPOSIT_AMOUNT = 100.0;
    private static final double MAX_DEPOSIT_LIMIT = 5000.00;
    private static final double MAX_DEPOSIT_LIMIT_MINUS_STEP = 4999.99;
    private static final double MIN_DEPOSIT_LIMIT = 0.01;
    private static final double MAX_DEPOSIT_LIMIT_PLUS_STEP = 5000.01;

    @Test
    public void shouldSuccessfullyDepositWhenCorrectData() {
        var user = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        DepositRequestDto depositDto = DepositRequestDto.builder()
                .id(user.getAccount().getId())
                .balance(BASE_DEPOSIT_AMOUNT)
                .build();

        var response = new DepositRequest(
                RequestSpecs.authSpec(user.getToken()),
                ResponseSpecs.successOk())
                .post(depositDto);

        DepositAssertions.assertDepositResponse(softly, response, user.getAccount().getId(),
                BASE_DEPOSIT_AMOUNT, INITIAL_BALANCE_NEW_ACCOUNT + BASE_DEPOSIT_AMOUNT);

        AccountAssertions.assertAccountBalance(softly, user.getToken(), user.getAccount().getId(),
                INITIAL_BALANCE_NEW_ACCOUNT + BASE_DEPOSIT_AMOUNT);

        TransactionAssertions.assertDepositTransaction(softly, user.getToken(), user.getAccount().getId(),
                BASE_DEPOSIT_AMOUNT, user.getAccount().getId());
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            MAX_DEPOSIT_LIMIT,
            MAX_DEPOSIT_LIMIT_MINUS_STEP,
            MIN_DEPOSIT_LIMIT
    })
    public void shouldDepositToAccountSuccessfullyWhenValidBoundaryValues(double amount) {
        var user = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        DepositRequestDto depositDto = DepositRequestDto.builder()
                .id(user.getAccount().getId())
                .balance(amount)
                .build();

        var response = new DepositRequest(
                RequestSpecs.authSpec(user.getToken()),
                ResponseSpecs.successOk())
                .post(depositDto);

        DepositAssertions.assertDepositResponse(softly, response, user.getAccount().getId(),
                amount, INITIAL_BALANCE_NEW_ACCOUNT + amount);

        AccountAssertions.assertAccountBalance(softly, user.getToken(), user.getAccount().getId(),
                INITIAL_BALANCE_NEW_ACCOUNT + amount);

        TransactionAssertions.assertDepositTransaction(softly, user.getToken(), user.getAccount().getId(),
                amount, user.getAccount().getId());
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            MAX_DEPOSIT_LIMIT_PLUS_STEP,
            ZERO_AMOUNT,
            NEGATIVE_AMOUNT,
    })
    public void shouldReturn400WhenAmountInvalidBoundaryValues(double amount) {
        var user = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        DepositRequestDto depositDto = DepositRequestDto.builder()
                .id(user.getAccount().getId())
                .balance(amount)
                .build();

       new DepositRequest(
                RequestSpecs.authSpec(user.getToken()),
                ResponseSpecs.badRequest())
                .post(depositDto);

        AccountAssertions.assertAccountBalance(
                softly, user.getToken(), user.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @Test
    public void shouldReturn403WhenDepositToAnotherUserId() {
        var userA = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);
        var userB = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        DepositRequestDto depositDto = DepositRequestDto.builder()
                .id(userB.getAccount().getId())
                .balance(BASE_DEPOSIT_AMOUNT)
                .build();

        new DepositRequest(
                RequestSpecs.authSpec(userA.getToken()),
                ResponseSpecs.forbidden())
                .post(depositDto);

        AccountAssertions.assertAccountBalance(
                softly, userA.getToken(), userA.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);

        AccountAssertions.assertAccountBalance(
                softly, userB.getToken(), userB.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @Test
    public void shouldReturn403WhenDepositToNonExistentUserId() {
        var user = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        DepositRequestDto depositDto = DepositRequestDto.builder()
                .id(NON_EXISTENT_USER_ID)
                .balance(BASE_DEPOSIT_AMOUNT)
                .build();

        new DepositRequest(
                RequestSpecs.authSpec(user.getToken()),
                ResponseSpecs.forbidden())
                .post(depositDto);

        AccountAssertions.assertAccountBalance(
                softly, user.getToken(), user.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @Test
    public void shouldReturn401WhenUnauthorizedUser() {
        var user = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        DepositRequestDto depositDto = DepositRequestDto.builder()
                .id(user.getAccount().getId())
                .balance(BASE_DEPOSIT_AMOUNT)
                .build();

        new DepositRequest(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.unauth())
                .post(depositDto);

        AccountAssertions.assertAccountBalance(
                softly, user.getToken(), user.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            AMOUNT_AS_STRING,
            AMOUNT_AS_NUMERIC_STRING
    })
    public void shouldReturn400WhenInvalidDataTypeInBalanceField(Object invalidAmount) {
        var user = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        Map<String, Object> depositBody = new HashMap<>();
        depositBody.put("accountId", user.getAccount().getId());
        depositBody.put("amount", invalidAmount);

        new DepositRequest(
                RequestSpecs.authSpec(user.getToken()),
                ResponseSpecs.badRequest())
                .post(depositBody);

        AccountAssertions.assertAccountBalance(
                softly, user.getToken(), user.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @Test
    public void shouldReturn400WithoutBalanceFieldInBody() {
        var user = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        Map<String, Object> depositBody = new HashMap<>();
        depositBody.put("accountId", user.getAccount().getId());

        new DepositRequest(
                RequestSpecs.authSpec(user.getToken()),
                ResponseSpecs.badRequest())
                .post(depositBody);

        AccountAssertions.assertAccountBalance(
                softly, user.getToken(), user.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @Test
    public void shouldReturn400WithoutIdFieldInBody() {
        var user = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        Map<String, Object> depositBody = new HashMap<>();
        depositBody.put("amount", BASE_DEPOSIT_AMOUNT);

        new DepositRequest(
                RequestSpecs.authSpec(user.getToken()),
                ResponseSpecs.badRequest())
                .post(depositBody);

        AccountAssertions.assertAccountBalance(
                softly, user.getToken(), user.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @Test
    public void shouldReturn400WhenRequestWithoutBody() {
        var user = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        new DepositRequest(
                RequestSpecs.authSpec(user.getToken()),
                ResponseSpecs.badRequest())
                .post();

        AccountAssertions.assertAccountBalance(
                softly, user.getToken(), user.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }
}
