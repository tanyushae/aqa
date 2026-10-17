package nbankApi.iteration2.middle.tests;

import nbankApi.iteration2.middle.assertions.TransactionAssertions;
import nbankApi.iteration2.middle.support.steps.SetupSteps;
import nbankApi.iteration2.middle.assertions.AccountAssertions;
import nbankApi.iteration2.middle.assertions.TransferAssertions;
import nbankApi.iteration2.middle.models.transfer.AccountTransferRequestDto;
import nbankApi.iteration2.middle.requests.transfer.AccountTransferRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static nbankApi.iteration2.middle.constants.ApiConstants.*;

import java.util.HashMap;
import java.util.Map;

public class TransferFromOneAccountToAnotherTest extends BaseTest {

    private static final double SETUP_DEPOSIT_AMOUNT = 5000.00;
    private static final double BASE_TRANSFER_AMOUNT = 100.00;
    private static final int DEPOSITS_COUNT = 3;
    private static final double INITIAL_BALANCE_FOR_BOUNDARY_TEST = SETUP_DEPOSIT_AMOUNT * DEPOSITS_COUNT;

    private static final double MAX_TRANSFER_LIMIT = 10000.00;
    private static final double MAX_TRANSFER_LIMIT_MINUS_STEP = 9999.99;
    private static final double MIN_TRANSFER_LIMIT = 0.01;

    private static final double MAX_TRANSFER_LIMIT_PLUS_STEP = 10000.01;
    private static final double EXCEEDING_AMOUNT_BALANCE = SETUP_DEPOSIT_AMOUNT + 10;

    @Test
    public void shouldTransferToAnotherUserSuccessfullyWhenAllDataValid() {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(sender.getAccount().getId())
                .receiverAccountId(receiver.getAccount().getId())
                .amount(BASE_TRANSFER_AMOUNT)
                .build();

        var response = new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.successOk())
                .post(transferRequestDto);

        TransferAssertions.assertTransferResponse(
                softly, response, sender.getAccount().getId(),
                BASE_TRANSFER_AMOUNT, receiver.getAccount().getId());

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(),
                SETUP_DEPOSIT_AMOUNT - BASE_TRANSFER_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(),
                BASE_TRANSFER_AMOUNT);

        TransactionAssertions.assertTransactionOut(
                softly, sender.getToken(), sender.getAccount().getId(),
                BASE_TRANSFER_AMOUNT, receiver.getAccount().getId());

        TransactionAssertions.assertTransactionIn(
                softly, receiver.getToken(), receiver.getAccount().getId(),
                BASE_TRANSFER_AMOUNT, sender.getAccount().getId());
    }

    @Test
    public void shouldTransferBetweenOwnAccountsSuccessfullyWhenAllDateValid() {
        var customer = SetupSteps.createCustomerWithTwoAccounts(adminToken, SETUP_DEPOSIT_AMOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(customer.getAccount1().getId())
                .receiverAccountId(customer.getAccount2().getId())
                .amount(BASE_TRANSFER_AMOUNT)
                .build();

        var response = new AccountTransferRequest(
                RequestSpecs.authSpec(customer.getToken()),
                ResponseSpecs.successOk())
                .post(transferRequestDto);

        TransferAssertions.assertTransferResponse(
                softly, response, customer.getAccount1().getId(),
                BASE_TRANSFER_AMOUNT, customer.getAccount2().getId());

        AccountAssertions.assertAccountBalance(
                softly, customer.getToken(), customer.getAccount1().getId(),
                SETUP_DEPOSIT_AMOUNT - BASE_TRANSFER_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, customer.getToken(), customer.getAccount2().getId(),
                BASE_TRANSFER_AMOUNT);

        TransactionAssertions.assertTransactionOut(
                softly, customer.getToken(), customer.getAccount1().getId(),
                BASE_TRANSFER_AMOUNT, customer.getAccount2().getId());

        TransactionAssertions.assertTransactionIn(
                softly, customer.getToken(), customer.getAccount2().getId(),
                BASE_TRANSFER_AMOUNT, customer.getAccount1().getId());
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            MAX_TRANSFER_LIMIT,
            MAX_TRANSFER_LIMIT_MINUS_STEP,
            MIN_TRANSFER_LIMIT
    })
    public void shouldTransferToAccountSuccessfullyWhenValidBoundaryValues(double amount) {
        var sender = SetupSteps.createAndFundAccountMultipleTimes(adminToken, SETUP_DEPOSIT_AMOUNT, DEPOSITS_COUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(sender.getAccount().getId())
                .receiverAccountId(receiver.getAccount().getId())
                .amount(amount)
                .build();

        var response = new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.successOk())
                .post(transferRequestDto);

        TransferAssertions.assertTransferResponse(
                softly, response, sender.getAccount().getId(), amount, receiver.getAccount().getId());

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(),
                INITIAL_BALANCE_FOR_BOUNDARY_TEST - amount);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), amount);

        TransactionAssertions.assertTransactionOut(
                softly, sender.getToken(), sender.getAccount().getId(),
                amount, receiver.getAccount().getId());

        TransactionAssertions.assertTransactionIn(
                softly, receiver.getToken(), receiver.getAccount().getId(),
                amount, sender.getAccount().getId());
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            MAX_TRANSFER_LIMIT_PLUS_STEP,
            ZERO_AMOUNT,
            NEGATIVE_AMOUNT
    })
    public void shouldReturn400TransferWithInvalidBoundaryValues(double amount) {
        var sender = SetupSteps.createAndFundAccountMultipleTimes(adminToken, SETUP_DEPOSIT_AMOUNT, DEPOSITS_COUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(sender.getAccount().getId())
                .receiverAccountId(receiver.getAccount().getId())
                .amount(amount)
                .build();

        new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.badRequest())
                .post(transferRequestDto);

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), INITIAL_BALANCE_FOR_BOUNDARY_TEST);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @Test
    public void shouldReturn400WhenTransferAmountExceedingCurrentBalance() {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(sender.getAccount().getId())
                .receiverAccountId(receiver.getAccount().getId())
                .amount(EXCEEDING_AMOUNT_BALANCE)
                .build();

        new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.badRequest())
                .post(transferRequestDto);

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            AMOUNT_AS_STRING,
            AMOUNT_AS_NUMERIC_STRING
    })
    public void shouldReturn400ForInvalidDataTypeInAmountField(String amount) {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, INITIAL_BALANCE_NEW_ACCOUNT);

        Map<String, Object> transferBody = new HashMap<>();
        transferBody.put("senderAccountId", sender.getAccount().getId());
        transferBody.put("receiverAccountId", receiver.getAccount().getId());
        transferBody.put("amount", amount);

        new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.badRequest())
                .post(transferBody);

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), INITIAL_BALANCE_NEW_ACCOUNT);
    }

    @Test
    public void shouldReturn403WhenTransferFromAnotherUserAccount() {
        var userA = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);
        var userB = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(userB.getAccount().getId())
                .receiverAccountId(userA.getAccount().getId())
                .amount(BASE_TRANSFER_AMOUNT)
                .build();

        new AccountTransferRequest(
                RequestSpecs.authSpec(userA.getToken()),
                ResponseSpecs.forbidden())
                .post(transferRequestDto);

        AccountAssertions.assertAccountBalance(
                softly, userA.getToken(), userA.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, userB.getToken(), userB.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);
    }

    @Test
    public void shouldReturn403WhenSenderAccountIdIsNotExist() {
        var receiver = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(NON_EXISTENT_USER_ID)
                .receiverAccountId(receiver.getAccount().getId())
                .amount(BASE_TRANSFER_AMOUNT)
                .build();

        new AccountTransferRequest(
                RequestSpecs.authSpec(receiver.getToken()),
                ResponseSpecs.forbidden())
                .post(transferRequestDto);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);
    }

    @Test
    public void shouldReturn400WhenReceiverAccountIdIsNotExist() {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(sender.getAccount().getId())
                .receiverAccountId(NON_EXISTENT_USER_ID)
                .amount(BASE_TRANSFER_AMOUNT)
                .build();

        new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.badRequest())
                .post(transferRequestDto);

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);
    }

    @Test
    public void shouldReturn401WhenSenderUnauthorized() {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(sender.getAccount().getId())
                .receiverAccountId(receiver.getAccount().getId())
                .amount(BASE_TRANSFER_AMOUNT)
                .build();

        new AccountTransferRequest(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.unauth()).post(transferRequestDto);

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);
    }

    @Test
    public void shouldReturn400WhenSenderIdAndReceiverIdTheSame() {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);

        AccountTransferRequestDto transferRequestDto = AccountTransferRequestDto.builder()
                .senderAccountId(sender.getAccount().getId())
                .receiverAccountId(sender.getAccount().getId())
                .amount(BASE_TRANSFER_AMOUNT)
                .build();

        new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.badRequest())
                .post(transferRequestDto);

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);
    }

    @Test
    public void shouldReturn400WhenRequestWithoutAmountFieldInBody() {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);

        Map<String, Object> transferBody = new HashMap<>();
        transferBody.put("senderAccountId", sender.getAccount().getId());
        transferBody.put("receiverAccountId", receiver.getAccount().getId());

        new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.badRequest())
                .post(transferBody);

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);
    }

    @Test
    public void shouldReturn400WhenRequestWithoutSenderAccountIdFieldInBody() {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);

        Map<String, Object> transferBody = new HashMap<>();
        transferBody.put("receiverAccountId", receiver.getAccount().getId());
        transferBody.put("amount", BASE_TRANSFER_AMOUNT);

        new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.badRequest())
                .post(transferBody);

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);
    }

    @Test
    public void shouldReturn400WhenRequestWithoutReceiverAccountIdFieldInBody() {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);

        Map<String, Object> transferBody = new HashMap<>();
        transferBody.put("senderAccountId", sender.getAccount().getId());
        transferBody.put("amount", BASE_TRANSFER_AMOUNT);

        new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.badRequest())
                .post(transferBody);

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);
    }

    @Test
    public void shouldReturn400WhenRequestWithoutBody() {
        var sender = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);
        var receiver = SetupSteps.createAndFundAccount(adminToken, SETUP_DEPOSIT_AMOUNT);

        new AccountTransferRequest(
                RequestSpecs.authSpec(sender.getToken()),
                ResponseSpecs.badRequest())
                .post();

        AccountAssertions.assertAccountBalance(
                softly, sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);

        AccountAssertions.assertAccountBalance(
                softly, receiver.getToken(), receiver.getAccount().getId(), SETUP_DEPOSIT_AMOUNT);
    }
}