package autotesting.nbankapi.iteration2.senior.tests;

import autotesting.nbankapi.iteration2.senior.assertions.AccountAssertions;
import autotesting.nbankapi.iteration2.senior.assertions.TransactionAssertions;
import autotesting.nbankapi.iteration2.senior.comparison.ModelAssertions;
import autotesting.nbankapi.iteration2.senior.customers.AccountSelector;
import autotesting.nbankapi.iteration2.senior.customers.CustomerWithAccount;
import autotesting.nbankapi.iteration2.senior.customers.TestParties;
import autotesting.nbankapi.iteration2.senior.models.account.CustomerAccountResponseDto;
import autotesting.nbankapi.iteration2.senior.models.account.TransactionDto;
import autotesting.nbankapi.iteration2.senior.models.deposit.DepositRequestDto;
import autotesting.nbankapi.iteration2.senior.models.deposit.DepositResponseDto;
import autotesting.nbankapi.iteration2.senior.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.senior.specs.ResponseSpecs;
import autotesting.nbankapi.iteration2.senior.steps.AccountSteps;
import autotesting.nbankapi.iteration2.senior.steps.DepositSteps;
import autotesting.nbankapi.iteration2.senior.steps.TestDataSteps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.ACCOUNT_ACCESS_DENIED_MESSAGE;
import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.DEPOSIT_ABOVE_MAX_MESSAGE;
import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.DEPOSIT_BELOW_MIN_MESSAGE;
import static autotesting.nbankapi.iteration2.senior.constants.TestData.INITIAL_BALANCE_NEW_ACCOUNT;
import static autotesting.nbankapi.iteration2.senior.constants.TestData.MAX_DEPOSIT_AMOUNT;
import static autotesting.nbankapi.iteration2.senior.constants.TestData.NEGATIVE_AMOUNT;
import static autotesting.nbankapi.iteration2.senior.constants.TestData.ZERO_AMOUNT;

public class UserDepositTest extends BaseTest {

    private static final double REGULAR_DEPOSIT_AMOUNT = 100.0;
    private static final double MAX_DEPOSIT_AMOUNT_MINUS_STEP = 4999.99;
    private static final double MIN_DEPOSIT_AMOUNT = 0.01;
    private static final double MAX_DEPOSIT_AMOUNT_PLUS_STEP = 5000.01;

    private static DepositRequestDto buildDepositRequest(int accountId, double depositAmount) {
        return DepositRequestDto.builder()
                .id(accountId)
                .balance(depositAmount)
                .build();
    }

    private static Stream<Arguments> validDepositCases() {
        return Stream.of(
                // caseName                                 depositAmount
                Arguments.of("typical: regular amount", REGULAR_DEPOSIT_AMOUNT),
                Arguments.of("boundary: min amount", MIN_DEPOSIT_AMOUNT),
                Arguments.of("boundary: max amount", MAX_DEPOSIT_AMOUNT),
                Arguments.of("boundary: max amount minus 0.01", MAX_DEPOSIT_AMOUNT_MINUS_STEP)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("validDepositCases")
    public void shouldDepositWhenAmountIsValid(String caseName, double depositAmount) {
        CustomerWithAccount user = TestDataSteps.createNewUserWithAccount();

        DepositRequestDto depositRequest = buildDepositRequest(user.getAccountId(), depositAmount);
        DepositResponseDto depositResponse = DepositSteps.deposit(user.getAuthSpec(), depositRequest);

        List<CustomerAccountResponseDto> userAccounts = AccountSteps.getAccounts(user.getAuthSpec());
        List<TransactionDto> userTransactions = AccountSteps.getTransactions(user.getAuthSpec(), user.getAccountId());

        ModelAssertions.assertThatModels(softly, depositRequest, depositResponse).match();
        softly.assertThat(depositResponse.getAccountNumber()).isEqualTo(user.getAccount().getAccountNumber());

        AccountAssertions.assertAccountBalance(softly, userAccounts, user.getAccountId(), INITIAL_BALANCE_NEW_ACCOUNT + depositAmount);
        TransactionAssertions.assertDepositTransaction(softly, userTransactions, user.getAccountId(), depositAmount);
    }

    private static Stream<Arguments> invalidDepositCases() {
        return Stream.of(
                // caseName                                    depositAmount                     expectedMessage
                Arguments.of("400: max amount plus 0.01", MAX_DEPOSIT_AMOUNT_PLUS_STEP, DEPOSIT_ABOVE_MAX_MESSAGE),
                Arguments.of("400: zero amount", ZERO_AMOUNT, DEPOSIT_BELOW_MIN_MESSAGE),
                Arguments.of("400: negative amount", NEGATIVE_AMOUNT, DEPOSIT_BELOW_MIN_MESSAGE)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("invalidDepositCases")
    public void shouldReturn400WhenDepositAmountIsInvalid(String caseName, double depositAmount, String expectedMessage) {
        CustomerWithAccount user = TestDataSteps.createNewUserWithAccount();

        DepositRequestDto depositRequest = buildDepositRequest(user.getAccountId(), depositAmount);

        DepositSteps.depositExpectingError(user.getAuthSpec(), depositRequest, ResponseSpecs.badRequest(expectedMessage));

        List<CustomerAccountResponseDto> userAccounts = AccountSteps.getAccounts(user.getAuthSpec());
        AccountAssertions.assertAccountBalance(softly, userAccounts, user.getAccountId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    private static Stream<Arguments> forbiddenDepositCases() {
        return Stream.of(
                // caseName                                  accountSelector
                Arguments.of("403: account of another user", AccountSelector.OTHER_USER_ACCOUNT),
                Arguments.of("403: account does not exist", AccountSelector.NON_EXISTENT_ACCOUNT)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("forbiddenDepositCases")
    public void shouldReturn403WhenDepositToForeignOrMissingAccount(String caseName, AccountSelector selector) {
        CustomerWithAccount userA = TestDataSteps.createNewUserWithAccount();
        CustomerWithAccount userB = TestDataSteps.createNewUserWithAccount();
        TestParties parties = new TestParties(userA, userB);

        DepositRequestDto depositRequest = buildDepositRequest(selector.resolveAccountId(parties), REGULAR_DEPOSIT_AMOUNT);

        DepositSteps.depositExpectingError(userA.getAuthSpec(), depositRequest, ResponseSpecs.forbidden(ACCOUNT_ACCESS_DENIED_MESSAGE));

        List<CustomerAccountResponseDto> accountsA = AccountSteps.getAccounts(userA.getAuthSpec());
        AccountAssertions.assertAccountBalance(softly, accountsA, userA.getAccountId(), INITIAL_BALANCE_NEW_ACCOUNT);

        List<CustomerAccountResponseDto> accountsB = AccountSteps.getAccounts(userB.getAuthSpec());
        AccountAssertions.assertAccountBalance(softly, accountsB, userB.getAccountId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @Test
    public void shouldReturn401WhenUnauthorizedUser() {
        CustomerWithAccount user = TestDataSteps.createNewUserWithAccount();

        DepositRequestDto depositRequest = buildDepositRequest(user.getAccountId(), REGULAR_DEPOSIT_AMOUNT);

        DepositSteps.depositExpectingError(RequestSpecs.unauthSpec(), depositRequest, ResponseSpecs.unauthorized());

        List<CustomerAccountResponseDto> userAccounts = AccountSteps.getAccounts(user.getAuthSpec());
        AccountAssertions.assertAccountBalance(softly, userAccounts, user.getAccountId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }
}
