package nbankApi.iteration2.middle.tests;

import nbankApi.iteration2.middle.assertions.AccountAssertions;
import nbankApi.iteration2.middle.assertions.TransactionAssertions;
import nbankApi.iteration2.middle.assertions.TransferAssertions;
import nbankApi.iteration2.middle.enums.AccountSelector;
import nbankApi.iteration2.middle.enums.ExpectedError;
import nbankApi.iteration2.middle.enums.RequestAuth;
import nbankApi.iteration2.middle.enums.TransferDirection;
import nbankApi.iteration2.middle.models.transfer.AccountTransferRequestDto;
import nbankApi.iteration2.middle.requests.transfer.AccountTransferRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;
import nbankApi.iteration2.middle.support.steps.SetupSteps;
import nbankApi.iteration2.middle.support.steps.TransferParticipants;
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
import static nbankApi.iteration2.middle.enums.TransferDirection.BETWEEN_OWN_ACCOUNTS;
import static nbankApi.iteration2.middle.enums.TransferDirection.TO_ANOTHER_USER;

public class TransferFromOneAccountToAnotherTest extends BaseTest {

    private static final double REGULAR_TRANSFER_AMOUNT = 100.00;
    private static final double MIN_TRANSFER_AMOUNT = 0.01;
    private static final double MAX_TRANSFER_AMOUNT = 10000.00;
    private static final double MAX_TRANSFER_AMOUNT_MINUS_CENT = 9999.99;
    private static final double MAX_TRANSFER_AMOUNT_PLUS_CENT = 10000.01;
    private static final double AMOUNT_EXCEEDING_ONE_DEPOSIT_BALANCE = SETUP_DEPOSIT_AMOUNT + 10;

    private static Stream<Arguments> validTransferCases() {
        return Stream.of(
                // caseName                                  transferAmount                   transferDirection
                Arguments.of("typical: to another user", REGULAR_TRANSFER_AMOUNT, TO_ANOTHER_USER),
                Arguments.of("typical: between own accounts", REGULAR_TRANSFER_AMOUNT, BETWEEN_OWN_ACCOUNTS),
                Arguments.of("boundary: min amount", MIN_TRANSFER_AMOUNT, TO_ANOTHER_USER),
                Arguments.of("boundary: max amount", MAX_TRANSFER_AMOUNT, TO_ANOTHER_USER),
                Arguments.of("boundary: max amount minus 0.01", MAX_TRANSFER_AMOUNT_MINUS_CENT, TO_ANOTHER_USER)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("validTransferCases")
    public void shouldTransferWhenRequestIsValid(String caseName, double transferAmount,
                                                 TransferDirection transferDirection) {
        TransferParticipants participants = transferDirection.prepareParticipants(adminToken);

        AccountTransferRequestDto transferDto = AccountTransferRequestDto.builder()
                .senderAccountId(participants.senderAccountId())
                .receiverAccountId(participants.receiverAccountId())
                .amount(transferAmount)
                .build();

        var transferResponse = new AccountTransferRequest(
                RequestSpecs.authSpec(participants.senderToken()),
                ResponseSpecs.successOk())
                .post(transferDto);

        TransferAssertions.assertTransferResponse(softly, transferResponse,
                participants.senderAccountId(), transferAmount, participants.receiverAccountId());

        AccountAssertions.assertAccountBalance(softly, participants.senderToken(),
                participants.senderAccountId(), participants.senderInitialBalance() - transferAmount);
        AccountAssertions.assertAccountBalance(softly, participants.receiverToken(),
                participants.receiverAccountId(), participants.receiverInitialBalance() + transferAmount);

        TransactionAssertions.assertTransferOutTransaction(softly, participants.senderToken(),
                participants.senderAccountId(), transferAmount, participants.receiverAccountId());
        TransactionAssertions.assertTransferInTransaction(softly, participants.receiverToken(),
                participants.receiverAccountId(), transferAmount, participants.senderAccountId());
    }

    private static Stream<Arguments> invalidTransferCases() {
        return Stream.of(
                // caseName                                 transferAmount                     currentUserDepositsCount  senderAccount   receiverAccount   requestAuth   expectedError
                Arguments.of("400: max amount plus 0.01", MAX_TRANSFER_AMOUNT_PLUS_CENT, SETUP_DEPOSITS_COUNT, OWN_ACCOUNT, ANOTHER_USER_ACCOUNT, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: zero amount", ZERO_AMOUNT, SETUP_DEPOSITS_COUNT, OWN_ACCOUNT, ANOTHER_USER_ACCOUNT, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: negative amount", NEGATIVE_AMOUNT, SETUP_DEPOSITS_COUNT, OWN_ACCOUNT, ANOTHER_USER_ACCOUNT, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: amount exceeds sender balance", AMOUNT_EXCEEDING_ONE_DEPOSIT_BALANCE, ONE_DEPOSIT, OWN_ACCOUNT, ANOTHER_USER_ACCOUNT, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: receiver account does not exist", REGULAR_TRANSFER_AMOUNT, ONE_DEPOSIT, OWN_ACCOUNT, NON_EXISTENT_ACCOUNT, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: sender and receiver are the same account", REGULAR_TRANSFER_AMOUNT, ONE_DEPOSIT, OWN_ACCOUNT, OWN_ACCOUNT, AUTHORIZED, BAD_REQUEST),
                Arguments.of("403: transfer from another user's account", REGULAR_TRANSFER_AMOUNT, ONE_DEPOSIT, ANOTHER_USER_ACCOUNT, OWN_ACCOUNT, AUTHORIZED, FORBIDDEN),
                Arguments.of("403: sender account does not exist", REGULAR_TRANSFER_AMOUNT, ONE_DEPOSIT, NON_EXISTENT_ACCOUNT, OWN_ACCOUNT, AUTHORIZED, FORBIDDEN),
                Arguments.of("401: request without token", REGULAR_TRANSFER_AMOUNT, ONE_DEPOSIT, OWN_ACCOUNT, ANOTHER_USER_ACCOUNT, RequestAuth.UNAUTHORIZED, ExpectedError.UNAUTHORIZED)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("invalidTransferCases")
    public void shouldNotTransferWhenRequestIsInvalid(String caseName, double transferAmount, int currentUserDepositsCount,
                                                      AccountSelector senderAccount, AccountSelector receiverAccount,
                                                      RequestAuth requestAuth, ExpectedError expectedError) {
        UserWithAccount currentUser = SetupSteps.createUserWithFundedAccount(adminToken, SETUP_DEPOSIT_AMOUNT, currentUserDepositsCount);
        UserWithAccount anotherUser = SetupSteps.createUserWithFundedAccount(adminToken, SETUP_DEPOSIT_AMOUNT, ONE_DEPOSIT);
        double currentUserInitialBalance = SETUP_DEPOSIT_AMOUNT * currentUserDepositsCount;
        double anotherUserInitialBalance = SETUP_DEPOSIT_AMOUNT * ONE_DEPOSIT;

        int ownAccountId = currentUser.getAccount().getId();
        int anotherUserAccountId = anotherUser.getAccount().getId();

        AccountTransferRequestDto transferDto = AccountTransferRequestDto.builder()
                .senderAccountId(senderAccount.selectAccountId(ownAccountId, anotherUserAccountId))
                .receiverAccountId(receiverAccount.selectAccountId(ownAccountId, anotherUserAccountId))
                .amount(transferAmount)
                .build();

        new AccountTransferRequest(requestAuth.requestSpec(currentUser.getToken()), expectedError.responseSpec())
                .post(transferDto);

        AccountAssertions.assertAccountBalance(softly, currentUser.getToken(), ownAccountId, currentUserInitialBalance);
        AccountAssertions.assertAccountBalance(softly, anotherUser.getToken(), anotherUserAccountId, anotherUserInitialBalance);
    }
}