package nbankApi.iteration2.middle.tests;

import nbankApi.iteration2.middle.assertions.AccountAssertions;
import nbankApi.iteration2.middle.assertions.DepositAssertions;
import nbankApi.iteration2.middle.assertions.TransactionAssertions;
import nbankApi.iteration2.middle.enums.AccountSelector;
import nbankApi.iteration2.middle.enums.ExpectedError;
import nbankApi.iteration2.middle.enums.RequestAuth;
import nbankApi.iteration2.middle.models.deposit.DepositRequestDto;
import nbankApi.iteration2.middle.requests.deposit.DepositRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;
import nbankApi.iteration2.middle.support.steps.SetupSteps;
import nbankApi.iteration2.middle.support.steps.UserWithAccount;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static nbankApi.iteration2.middle.constants.ApiConstants.*;
import static nbankApi.iteration2.middle.enums.AccountSelector.*;
import static nbankApi.iteration2.middle.enums.ExpectedError.BAD_REQUEST;
import static nbankApi.iteration2.middle.enums.ExpectedError.FORBIDDEN;
import static nbankApi.iteration2.middle.enums.RequestAuth.AUTHORIZED;

public class UserDepositTest extends BaseTest {

    private static final double REGULAR_DEPOSIT_AMOUNT = 100.00;
    private static final double MIN_DEPOSIT_AMOUNT = 0.01;
    private static final double MAX_DEPOSIT_AMOUNT = 5000.00;
    private static final double MAX_DEPOSIT_AMOUNT_MINUS_CENT = 4999.99;
    private static final double MAX_DEPOSIT_AMOUNT_PLUS_CENT = 5000.01;

    private static Stream<Arguments> validDepositCases() {
        return Stream.of(
                // caseName                                 depositAmount
                Arguments.of("typical: regular amount", REGULAR_DEPOSIT_AMOUNT),
                Arguments.of("boundary: min amount", MIN_DEPOSIT_AMOUNT),
                Arguments.of("boundary: max amount", MAX_DEPOSIT_AMOUNT),
                Arguments.of("boundary: max amount minus 0.01", MAX_DEPOSIT_AMOUNT_MINUS_CENT)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("validDepositCases")
    public void shouldDepositWhenAmountIsValid(String caseName, double depositAmount) {
        UserWithAccount user = SetupSteps.createUserWithEmptyAccount(adminToken);
        int accountId = user.getAccount().getId();

        DepositRequestDto depositDto = DepositRequestDto.builder()
                .id(accountId)
                .balance(depositAmount) // поле API называется balance, но содержит сумму пополнения
                .build();

        var depositResponse = new DepositRequest(
                RequestSpecs.authSpec(user.getToken()),
                ResponseSpecs.successOk())
                .post(depositDto);

        DepositAssertions.assertDepositResponse(softly, depositResponse, accountId,
                depositAmount, NEW_ACCOUNT_BALANCE + depositAmount);

        AccountAssertions.assertAccountBalance(softly, user.getToken(), accountId,
                NEW_ACCOUNT_BALANCE + depositAmount);

        TransactionAssertions.assertDepositTransaction(softly, user.getToken(), accountId,
                depositAmount, accountId);
    }

    private static Stream<Arguments> invalidDepositCases() {
        return Stream.of(
                // caseName                                    depositAmount                 targetAccount    requestAuth    xpectedError
                Arguments.of("400: max amount plus 0.01", MAX_DEPOSIT_AMOUNT_PLUS_CENT, OWN_ACCOUNT, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: zero amount", ZERO_AMOUNT, OWN_ACCOUNT, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: negative amount", NEGATIVE_AMOUNT, OWN_ACCOUNT, AUTHORIZED, BAD_REQUEST),
                Arguments.of("403: deposit to another user's account", REGULAR_DEPOSIT_AMOUNT, ANOTHER_USER_ACCOUNT, AUTHORIZED, FORBIDDEN),
                Arguments.of("403: deposit to non-existent account", REGULAR_DEPOSIT_AMOUNT, NON_EXISTENT_ACCOUNT, AUTHORIZED, FORBIDDEN),
                Arguments.of("401: request without token", REGULAR_DEPOSIT_AMOUNT, OWN_ACCOUNT, RequestAuth.UNAUTHORIZED, ExpectedError.UNAUTHORIZED)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("invalidDepositCases")
    public void shouldNotDepositWhenRequestIsInvalid(String caseName, double depositAmount, AccountSelector targetAccount,
                                                     RequestAuth requestAuth, ExpectedError expectedError) {
        UserWithAccount currentUser = SetupSteps.createUserWithEmptyAccount(adminToken);
        UserWithAccount anotherUser = SetupSteps.createUserWithEmptyAccount(adminToken);
        int ownAccountId = currentUser.getAccount().getId();
        int anotherUserAccountId = anotherUser.getAccount().getId();

        DepositRequestDto depositDto = DepositRequestDto.builder()
                .id(targetAccount.selectAccountId(ownAccountId, anotherUserAccountId))
                .balance(depositAmount)
                .build();

        new DepositRequest(requestAuth.requestSpec(currentUser.getToken()), expectedError.responseSpec())
                .post(depositDto);

        AccountAssertions.assertAccountBalance(softly, currentUser.getToken(), ownAccountId, NEW_ACCOUNT_BALANCE);
        AccountAssertions.assertAccountBalance(softly, anotherUser.getToken(), anotherUserAccountId, NEW_ACCOUNT_BALANCE);
    }
}
