package autotesting.nbankapi.iteration2.senior.tests;

import autotesting.nbankapi.iteration2.senior.assertions.AccountAssertions;
import autotesting.nbankapi.iteration2.senior.assertions.TransactionAssertions;
import autotesting.nbankapi.iteration2.senior.comparison.ModelAssertions;
import autotesting.nbankapi.iteration2.senior.customers.AccountSelector;
import autotesting.nbankapi.iteration2.senior.customers.CustomerWithAccount;
import autotesting.nbankapi.iteration2.senior.customers.CustomerWithTwoAccounts;
import autotesting.nbankapi.iteration2.senior.customers.TestParties;
import autotesting.nbankapi.iteration2.senior.models.account.CustomerAccountResponseDto;
import autotesting.nbankapi.iteration2.senior.models.account.TransactionDto;
import autotesting.nbankapi.iteration2.senior.models.transfer.AccountTransferRequestDto;
import autotesting.nbankapi.iteration2.senior.models.transfer.AccountTransferResponseDto;
import autotesting.nbankapi.iteration2.senior.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.senior.specs.ResponseSpecs;
import autotesting.nbankapi.iteration2.senior.steps.AccountSteps;
import autotesting.nbankapi.iteration2.senior.steps.TestDataSteps;
import autotesting.nbankapi.iteration2.senior.steps.TransferSteps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.ACCOUNT_ACCESS_DENIED_MESSAGE;
import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.TRANSFER_ABOVE_MAX_MESSAGE;
import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.TRANSFER_BELOW_MIN_MESSAGE;
import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.TRANSFER_INVALID_MESSAGE;
import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.TRANSFER_SUCCESS_MESSAGE;
import static autotesting.nbankapi.iteration2.senior.constants.TestData.ACCOUNT_FUNDING_AMOUNT;
import static autotesting.nbankapi.iteration2.senior.constants.TestData.INITIAL_BALANCE_NEW_ACCOUNT;
import static autotesting.nbankapi.iteration2.senior.constants.TestData.NEGATIVE_AMOUNT;
import static autotesting.nbankapi.iteration2.senior.constants.TestData.ZERO_AMOUNT;


public class TransferFromOneAccountToAnotherTest extends BaseTest {

    private static final double REGULAR_TRANSFER_AMOUNT = 100.00;
    // Лимит одного депозита 5000, а перевод до 10000: три депозита дают 15000 на счёте отправителя
    private static final int DEPOSITS_COUNT = 3;
    private static final double SENDER_INITIAL_BALANCE = ACCOUNT_FUNDING_AMOUNT * DEPOSITS_COUNT;
    private static final double MIN_TRANSFER_AMOUNT = 0.01;
    private static final double MAX_TRANSFER_AMOUNT = 10000.00;
    private static final double MAX_TRANSFER_AMOUNT_MINUS_STEP = 9999.99;
    private static final double MAX_TRANSFER_AMOUNT_PLUS_STEP = 10000.01;
    private static final double EXCESS_OVER_BALANCE = 10.00;
    private static final double AMOUNT_EXCEEDING_BALANCE = ACCOUNT_FUNDING_AMOUNT + EXCESS_OVER_BALANCE;

    private static AccountTransferRequestDto buildTransferRequest(int senderId, int receiverId, double amount) {
        return AccountTransferRequestDto.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(amount)
                .build();
    }

    private static Stream<Arguments> validTransferCases() {
        return Stream.of(
                // caseName                                 transferAmount
                Arguments.of("typical: regular amount", REGULAR_TRANSFER_AMOUNT),
                Arguments.of("boundary: min amount", MIN_TRANSFER_AMOUNT),
                Arguments.of("boundary: max amount", MAX_TRANSFER_AMOUNT),
                Arguments.of("boundary: max amount minus 0.01", MAX_TRANSFER_AMOUNT_MINUS_STEP)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("validTransferCases")
    public void shouldTransferWhenAmountIsValid(String caseName, double transferAmount) {
        CustomerWithAccount sender = TestDataSteps.createNewUserWithFundedAccount(ACCOUNT_FUNDING_AMOUNT, DEPOSITS_COUNT);
        CustomerWithAccount receiver = TestDataSteps.createNewUserWithAccount();

        AccountTransferRequestDto transferRequest = buildTransferRequest(sender.getAccountId(), receiver.getAccountId(), transferAmount);
        AccountTransferResponseDto transferResponse = TransferSteps.transfer(sender.getAuthSpec(), transferRequest);

        List<CustomerAccountResponseDto> senderAccounts = AccountSteps.getAccounts(sender.getAuthSpec());
        List<CustomerAccountResponseDto> receiverAccounts = AccountSteps.getAccounts(receiver.getAuthSpec());
        List<TransactionDto> senderTransactions = AccountSteps.getTransactions(sender.getAuthSpec(), sender.getAccountId());
        List<TransactionDto> receiverTransactions = AccountSteps.getTransactions(receiver.getAuthSpec(), receiver.getAccountId());

        ModelAssertions.assertThatModels(softly, transferRequest, transferResponse).match();
        softly.assertThat(transferResponse.getMessage()).isEqualTo(TRANSFER_SUCCESS_MESSAGE);

        AccountAssertions.assertAccountBalance(softly, senderAccounts, sender.getAccountId(), SENDER_INITIAL_BALANCE - transferAmount);
        AccountAssertions.assertAccountBalance(softly, receiverAccounts, receiver.getAccountId(), INITIAL_BALANCE_NEW_ACCOUNT + transferAmount);

        TransactionAssertions.assertTransferOutTransaction(softly, senderTransactions, receiver.getAccountId(), transferAmount);
        TransactionAssertions.assertTransferInTransaction(softly, receiverTransactions, sender.getAccountId(), transferAmount);
    }

    @Test
    public void shouldTransferBetweenOwnAccountsWhenAmountIsValid() {
        CustomerWithTwoAccounts customer = TestDataSteps.createNewUserWithTwoAccountsAndFundedFirst(ACCOUNT_FUNDING_AMOUNT);

        AccountTransferRequestDto transferRequest = buildTransferRequest(customer.getFirstAccountId(), customer.getSecondAccountId(), REGULAR_TRANSFER_AMOUNT);
        AccountTransferResponseDto transferResponse = TransferSteps.transfer(customer.getAuthSpec(), transferRequest);

        List<CustomerAccountResponseDto> accounts = AccountSteps.getAccounts(customer.getAuthSpec());
        List<TransactionDto> firstAccountTransactions = AccountSteps.getTransactions(customer.getAuthSpec(), customer.getFirstAccountId());
        List<TransactionDto> secondAccountTransactions = AccountSteps.getTransactions(customer.getAuthSpec(), customer.getSecondAccountId());

        ModelAssertions.assertThatModels(softly, transferRequest, transferResponse).match();
        softly.assertThat(transferResponse.getMessage()).isEqualTo(TRANSFER_SUCCESS_MESSAGE);

        AccountAssertions.assertAccountBalance(softly, accounts, customer.getFirstAccountId(), ACCOUNT_FUNDING_AMOUNT - REGULAR_TRANSFER_AMOUNT);
        AccountAssertions.assertAccountBalance(softly, accounts, customer.getSecondAccountId(), REGULAR_TRANSFER_AMOUNT);

        TransactionAssertions.assertTransferOutTransaction(softly, firstAccountTransactions, customer.getSecondAccountId(), REGULAR_TRANSFER_AMOUNT);
        TransactionAssertions.assertTransferInTransaction(softly, secondAccountTransactions, customer.getFirstAccountId(), REGULAR_TRANSFER_AMOUNT);
    }

    private static Stream<Arguments> invalidTransferCases() {
        return Stream.of(
                // caseName                                  selector,                   amount                                   expectedMessage
                Arguments.of("400: max amount plus 0.01", AccountSelector.OTHER_USER_ACCOUNT, MAX_TRANSFER_AMOUNT_PLUS_STEP, TRANSFER_ABOVE_MAX_MESSAGE),
                Arguments.of("400: zero amount", AccountSelector.OTHER_USER_ACCOUNT, ZERO_AMOUNT, TRANSFER_BELOW_MIN_MESSAGE),
                Arguments.of("400: negative amount", AccountSelector.OTHER_USER_ACCOUNT, NEGATIVE_AMOUNT, TRANSFER_BELOW_MIN_MESSAGE),
                Arguments.of("400: amount exceeds balance", AccountSelector.OTHER_USER_ACCOUNT, AMOUNT_EXCEEDING_BALANCE, TRANSFER_INVALID_MESSAGE),
                Arguments.of("400: same sender and receiver", AccountSelector.OWN_ACCOUNT, REGULAR_TRANSFER_AMOUNT, TRANSFER_INVALID_MESSAGE),
                Arguments.of("400: receiver does not exist", AccountSelector.NON_EXISTENT_ACCOUNT, REGULAR_TRANSFER_AMOUNT, TRANSFER_INVALID_MESSAGE)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("invalidTransferCases")
    public void shouldReturn400WhenTransferRequestIsInvalid(String caseName, AccountSelector receiverSelector, double transferAmount, String expectedMessage) {
        CustomerWithAccount sender = TestDataSteps.createNewUserWithFundedAccount(ACCOUNT_FUNDING_AMOUNT);
        CustomerWithAccount receiver = TestDataSteps.createNewUserWithAccount();
        TestParties parties = new TestParties(sender, receiver);

        AccountTransferRequestDto transferRequest =
                buildTransferRequest(sender.getAccountId(), receiverSelector.resolveAccountId(parties), transferAmount);

        TransferSteps.transferExpectingError(sender.getAuthSpec(), transferRequest, ResponseSpecs.badRequest(expectedMessage));

        List<CustomerAccountResponseDto> senderAccounts = AccountSteps.getAccounts(sender.getAuthSpec());
        AccountAssertions.assertAccountBalance(softly, senderAccounts, sender.getAccountId(), ACCOUNT_FUNDING_AMOUNT);

        List<CustomerAccountResponseDto> receiverAccounts = AccountSteps.getAccounts(receiver.getAuthSpec());
        AccountAssertions.assertAccountBalance(softly, receiverAccounts, receiver.getAccountId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    private static Stream<Arguments> forbiddenTransferCases() {
        return Stream.of(
                // caseName                                    accountSelector
                Arguments.of("403: sender account of another user", AccountSelector.OTHER_USER_ACCOUNT),
                Arguments.of("403: sender account does not exist", AccountSelector.NON_EXISTENT_ACCOUNT)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("forbiddenTransferCases")
    public void shouldReturn403WhenTransferFromForeignOrMissingSenderAccount(String caseName, AccountSelector senderSelector) {
        CustomerWithAccount requester = TestDataSteps.createNewUserWithFundedAccount(ACCOUNT_FUNDING_AMOUNT);
        CustomerWithAccount anotherCustomer = TestDataSteps.createNewUserWithFundedAccount(ACCOUNT_FUNDING_AMOUNT);
        TestParties parties = new TestParties(requester, anotherCustomer);

        AccountTransferRequestDto transferRequest =
                buildTransferRequest(senderSelector.resolveAccountId(parties), requester.getAccountId(), REGULAR_TRANSFER_AMOUNT);

        TransferSteps.transferExpectingError(requester.getAuthSpec(), transferRequest, ResponseSpecs.forbidden(ACCOUNT_ACCESS_DENIED_MESSAGE));

        List<CustomerAccountResponseDto> requesterAccounts = AccountSteps.getAccounts(requester.getAuthSpec());
        AccountAssertions.assertAccountBalance(softly, requesterAccounts, requester.getAccountId(), ACCOUNT_FUNDING_AMOUNT);

        List<CustomerAccountResponseDto> anotherCustomerAccounts = AccountSteps.getAccounts(anotherCustomer.getAuthSpec());
        AccountAssertions.assertAccountBalance(softly, anotherCustomerAccounts, anotherCustomer.getAccountId(), ACCOUNT_FUNDING_AMOUNT);
    }

    @Test
    public void shouldReturn401WhenSenderUnauthorized() {
        CustomerWithAccount sender = TestDataSteps.createNewUserWithFundedAccount(ACCOUNT_FUNDING_AMOUNT);
        CustomerWithAccount receiver = TestDataSteps.createNewUserWithFundedAccount(ACCOUNT_FUNDING_AMOUNT);

        AccountTransferRequestDto transferRequest = buildTransferRequest(sender.getAccountId(), receiver.getAccountId(), REGULAR_TRANSFER_AMOUNT);

        TransferSteps.transferExpectingError(RequestSpecs.unauthSpec(), transferRequest, ResponseSpecs.unauthorized());

        List<CustomerAccountResponseDto> senderAccounts = AccountSteps.getAccounts(sender.getAuthSpec());
        List<CustomerAccountResponseDto> receiverAccounts = AccountSteps.getAccounts(receiver.getAuthSpec());

        AccountAssertions.assertAccountBalance(softly, senderAccounts, sender.getAccountId(), ACCOUNT_FUNDING_AMOUNT);
        AccountAssertions.assertAccountBalance(softly, receiverAccounts, receiver.getAccountId(), ACCOUNT_FUNDING_AMOUNT);
    }
}
