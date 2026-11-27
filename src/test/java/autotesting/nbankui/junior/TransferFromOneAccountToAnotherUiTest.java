package autotesting.nbankui.junior;

import autotesting.nbankapi.iteration2.middle.assertions.AccountAssertions;
import autotesting.nbankapi.iteration2.middle.support.steps.SetupSteps;
import autotesting.nbankapi.iteration2.middle.support.steps.UserWithAccount;
import autotesting.nbankapi.iteration2.middle.support.steps.UserWithTwoAccounts;
import autotesting.nbankui.junior.enums.ConfirmCheckbox;
import autotesting.nbankui.junior.enums.RecipientAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;

import java.util.stream.Stream;

import static autotesting.nbankapi.iteration2.middle.constants.ApiConstants.USER_PASSWORD;
import static autotesting.nbankui.junior.UiConstants.ACCOUNT_OPTION_TEMPLATE;
import static autotesting.nbankui.junior.UiConstants.MAKE_A_TRANSFER_TEXT;
import static autotesting.nbankui.junior.UiConstants.NONAME_TEXT;
import static autotesting.nbankui.junior.UiConstants.TRANSFER_SUCCESS_TEMPLATE;
import static autotesting.nbankui.junior.UiConstants.ZERO_BALANCE_UI;
import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.checked;
import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.refresh;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferFromOneAccountToAnotherUiTest extends BaseUiTest {
    private static final String RECIPIENT_NAME_INPUT = "input[placeholder='Enter recipient name']";
    private static final String RECIPIENT_ACCOUNT_NUMBER_INPUT = "input[placeholder='Enter recipient account number']";
    private static final String CONFIRM_CHECKBOX = "input[id=confirmCheck]";

    private static final String NEW_TRANSFER_BUTTON_TEXT = "New Transfer";
    private static final String TRANSFER_AGAIN_BUTTON_TEXT = "Transfer Again";
    private static final String SELECT_ACCOUNT_TEXT = "Select Your Account:";
    private static final String RECIPIENT_NAME_TEXT = "Recipient Name:";
    private static final String RECIPIENT_ACCOUNT_NUMBER_TEXT = "Recipient Account Number:";
    private static final String AMOUNT_TEXT = "Amount:";
    private static final String CONFIRM_DETAILS_TEXT = "Confirm details are correct";
    private static final String SEND_TRANSFER_BUTTON_TEXT = "Send Transfer";
    private static final String LOGOUT_BUTTON_TEXT = "Logout";

    private static final String REGULAR_TRANSFER_AMOUNT = "100";
    private static final String SETUP_BALANCE_UI = "1000.00";
    private static final String REGULAR_SENDER_BALANCE_UI = "900.00";
    private static final String REGULAR_RECIPIENT_BALANCE_UI = "100.00";
    private static final String MAX_TRANSFER_AMOUNT_PLUS_STEP = "10000.01";
    private static final String BALANCE_ABOVE_TRANSFER_LIMIT_UI = "10010.00";
    private static final String FLOAT_TRANSFER_AMOUNT = "10.10";
    private static final String FLOAT_SENDER_BALANCE_UI = "989.90";
    private static final String FLOAT_RECIPIENT_BALANCE_UI = "10.10";
    private static final String AMOUNT_ABOVE_BALANCE = "1001";
    private static final String ZERO_TRANSFER_AMOUNT = "0";

    private static final int SETUP_DEPOSIT_AMOUNT = 1000;
    private static final int SETUP_DEPOSIT_COUNT = 1;
    // TR-05: баланс 10010 = два депозита по лимиту и один маленький
    private static final int DEPOSIT_AT_LIMIT_AMOUNT = 5000;
    private static final int DEPOSIT_AT_LIMIT_COUNT = 2;
    private static final int SMALL_DEPOSIT_AMOUNT = 10;
    private static final int SMALL_DEPOSIT_COUNT = 1;
    // Опции в списке "Select Your Account": placeholder "-- Choose an account --" + счета
    private static final int ONE_ACCOUNT_OPTIONS_COUNT = 2;
    private static final int TWO_ACCOUNT_OPTIONS_COUNT = 3;

    private static final String TRANSFER_INVALID_DATA_MESSAGE = "❌ Please fill all fields and confirm.";
    private static final String TRANSFER_NOT_FOUND_ACCOUNT_MESSAGE = "❌ No user found with this account number.";
    private static final String TRANSFER_ABOVE_LIMIT_MESSAGE = "❌ Error: Transfer amount cannot exceed 10000";
    private static final String TRANSFER_INSUFFICIENT_FUNDS_MESSAGE =
            "❌ Error: Invalid transfer: insufficient funds or invalid accounts";
    private static final String TRANSFER_ZERO_AMOUNT_MESSAGE = "❌ Error: Transfer amount must be at least 0.01";

    private static Stream<Arguments> validTransferCases() {
        return Stream.of(
                // caseName | amount | senderBalance | recipientBalance
                Arguments.of("regular amount", REGULAR_TRANSFER_AMOUNT,
                        REGULAR_SENDER_BALANCE_UI, REGULAR_RECIPIENT_BALANCE_UI),
                Arguments.of("float amount", FLOAT_TRANSFER_AMOUNT,
                        FLOAT_SENDER_BALANCE_UI, FLOAT_RECIPIENT_BALANCE_UI)
        );
    }

    @DisplayName("UI-TR-01. Успешный перевод другому пользователю")
    @ParameterizedTest(name = "{0}")
    @MethodSource("validTransferCases")
    public void shouldTransferToOtherUserWhenAllDataValid(String caseName, String amount, String senderBalance,
                                                          String recipientBalance) {
        // Предусловия (API)
        UserWithAccount userA = SetupSteps.createUserWithFundedAccount(adminToken,
                SETUP_DEPOSIT_AMOUNT, SETUP_DEPOSIT_COUNT);
        UserWithAccount userB = SetupSteps.createUserWithEmptyAccount(adminToken);
        String userBName = getUsername(userB.getToken());
        String recipientAccount = userB.getAccount().getAccountNumber();
        // Предусловия (UI)
        String userAName = getUsername(userA.getToken());
        loginAs(userAName, USER_PASSWORD);
        checkHeader(NONAME_TEXT, userAName);
        // Шаг 1
        openTransferForm();
        // Шаг 2
        String acc1Option = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getAccount().getAccountNumber(), SETUP_BALANCE_UI);
        $(ACCOUNT_SELECT).$$(OPTION).shouldHave(size(ONE_ACCOUNT_OPTIONS_COUNT));
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(CHOOSE_ACCOUNT_OPTION_TEXT)).should(exist);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(acc1Option)).should(exist);
        // Шаг 3
        $(ACCOUNT_SELECT).selectOption(acc1Option);
        $(ACCOUNT_SELECT).getSelectedOption().shouldHave(exactText(acc1Option));
        // Шаг 4–8
        makeTransfer(recipientAccount, amount);
        // Шаг 9
        refresh();
        String acc1OptionAfterTransfer = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getAccount().getAccountNumber(), senderBalance);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(acc1OptionAfterTransfer)).should(exist);
        // Шаг 10
        $$(BUTTON).findBy(text(LOGOUT_BUTTON_TEXT)).click();
        loginAs(userBName, USER_PASSWORD);
        checkHeader(NONAME_TEXT, userBName);
        $(withText(MAKE_A_TRANSFER_TEXT)).click();
        $$(PAGE_TITLE).findBy(text(MAKE_A_TRANSFER_TEXT)).shouldBe(visible);
        String userBAccOptionAfterTransfer = ACCOUNT_OPTION_TEMPLATE.formatted(
                recipientAccount, recipientBalance);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(userBAccOptionAfterTransfer)).should(exist);
        // Шаг 11: проверка балансов на сервере через API
        AccountAssertions.assertAccountBalance(softly, userA.getToken(),
                userA.getAccount().getId(), Double.parseDouble(senderBalance));
        AccountAssertions.assertAccountBalance(softly, userB.getToken(),
                userB.getAccount().getId(), Double.parseDouble(recipientBalance));
    }

    @DisplayName("UI-TR-02. Успешный перевод на собственный счет")
    @Test
    public void shouldTransferToOwnAccountWhenAllDataValid() {
        // Предусловия (API)
        UserWithTwoAccounts userA = SetupSteps.createUserWithTwoAccounts(adminToken,
                SETUP_DEPOSIT_AMOUNT, SETUP_DEPOSIT_COUNT);
        String recipientAccount = userA.getSecondAccount().getAccountNumber();
        // Предусловия (UI)
        String userAName = getUsername(userA.getToken());
        loginAs(userAName, USER_PASSWORD);
        checkHeader(NONAME_TEXT, userAName);
        // Шаг 1
        openTransferForm();
        // Шаг 2
        String firstAccOption = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getFirstAccount().getAccountNumber(), SETUP_BALANCE_UI);
        String secondAccOption = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getSecondAccount().getAccountNumber(), ZERO_BALANCE_UI);
        $(ACCOUNT_SELECT).$$(OPTION).shouldHave(size(TWO_ACCOUNT_OPTIONS_COUNT));
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(CHOOSE_ACCOUNT_OPTION_TEXT)).should(exist);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(firstAccOption)).should(exist);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(secondAccOption)).should(exist);
        // Шаг 3
        $(ACCOUNT_SELECT).selectOption(firstAccOption);
        $(ACCOUNT_SELECT).getSelectedOption().shouldHave(exactText(firstAccOption));
        // Шаг 4–8
        makeTransfer(recipientAccount, REGULAR_TRANSFER_AMOUNT);
        // Шаг 9
        refresh();
        String firstAccOptionAfterTransfer = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getFirstAccount().getAccountNumber(), REGULAR_SENDER_BALANCE_UI);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(firstAccOptionAfterTransfer)).should(exist);
        // Шаг 10
        String secondAccOptionAfterTransfer = ACCOUNT_OPTION_TEMPLATE.formatted(
                recipientAccount, REGULAR_RECIPIENT_BALANCE_UI);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(secondAccOptionAfterTransfer)).should(exist);
        // Шаг 11: проверка балансов на сервере через API
        AccountAssertions.assertAccountBalance(softly, userA.getToken(),
                userA.getFirstAccount().getId(), Double.parseDouble(REGULAR_SENDER_BALANCE_UI));
        AccountAssertions.assertAccountBalance(softly, userA.getToken(),
                userA.getSecondAccount().getId(), Double.parseDouble(REGULAR_RECIPIENT_BALANCE_UI));
    }

    private static Stream<Arguments> invalidFormTransferCases() {
        return Stream.of(
                // caseName | recipient | amount | checkbox | expectedMessage
                Arguments.of("checkbox not confirmed", RecipientAccount.ANOTHER_USER,
                        REGULAR_TRANSFER_AMOUNT, ConfirmCheckbox.UNCHECKED, TRANSFER_INVALID_DATA_MESSAGE),
                Arguments.of("non-existent account", RecipientAccount.NON_EXISTENT,
                        REGULAR_TRANSFER_AMOUNT, ConfirmCheckbox.CHECKED, TRANSFER_NOT_FOUND_ACCOUNT_MESSAGE)
        );
    }

    @DisplayName("UI-TR-03. Перевод с некорректно заполненной формой")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidFormTransferCases")
    public void shouldNotTransferWhenFormIsInvalid(String caseName, RecipientAccount recipient, String amount,
                                                   ConfirmCheckbox confirmCheckbox, String expectedMessage) {
        // Предусловия (API)
        UserWithAccount userA = SetupSteps.createUserWithFundedAccount(adminToken,
                SETUP_DEPOSIT_AMOUNT, SETUP_DEPOSIT_COUNT);
        UserWithAccount userB = SetupSteps.createUserWithEmptyAccount(adminToken);
        // Предусловия (UI)
        String userAName = getUsername(userA.getToken());
        loginAs(userAName, USER_PASSWORD);
        checkHeader(NONAME_TEXT, userAName);
        // Шаг 1
        openTransferForm();
        // Шаг 2
        String acc1Option = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getAccount().getAccountNumber(), SETUP_BALANCE_UI);
        $(ACCOUNT_SELECT).$$(OPTION).shouldHave(size(ONE_ACCOUNT_OPTIONS_COUNT));
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(CHOOSE_ACCOUNT_OPTION_TEXT)).should(exist);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(acc1Option)).should(exist);
        // Шаг 3
        $(ACCOUNT_SELECT).selectOption(acc1Option);
        $(ACCOUNT_SELECT).getSelectedOption().shouldHave(exactText(acc1Option));
        // Шаг 4
        String recipientAccount = recipient.accountNumber(userB);
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).setValue(recipientAccount);
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).shouldHave(value(recipientAccount));
        $(ENTER_AMOUNT_INPUT).setValue(amount);
        $(ENTER_AMOUNT_INPUT).shouldHave(value(amount));
        confirmCheckbox.apply($(CONFIRM_CHECKBOX));
        // Шаг 5
        $$(BUTTON).findBy(text(SEND_TRANSFER_BUTTON_TEXT)).click();
        Alert alert = switchTo().alert();
        assertEquals(expectedMessage, alert.getText());
        // Шаг 6
        alert.accept();
        refresh();
        String acc1OptionAfterAttempt = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getAccount().getAccountNumber(), SETUP_BALANCE_UI);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(acc1OptionAfterAttempt)).should(exist);
        // Шаг 7: проверка балансов на сервере через API
        AccountAssertions.assertAccountBalance(softly, userA.getToken(),
                userA.getAccount().getId(), Double.parseDouble(SETUP_BALANCE_UI));
        AccountAssertions.assertAccountBalance(softly, userB.getToken(),
                userB.getAccount().getId(), Double.parseDouble(ZERO_BALANCE_UI));
    }

    private static Stream<Arguments> serverRejectedTransferCases() {
        return Stream.of(
                // caseName | amount | expectedMessage
                Arguments.of("amount above balance", AMOUNT_ABOVE_BALANCE, TRANSFER_INSUFFICIENT_FUNDS_MESSAGE),
                Arguments.of("zero amount", ZERO_TRANSFER_AMOUNT, TRANSFER_ZERO_AMOUNT_MESSAGE)
        );
    }

    @DisplayName("UI-TR-04. Перевод, отклоненный сервером")
    @ParameterizedTest(name = "{0}")
    @MethodSource("serverRejectedTransferCases")
    public void shouldShowErrorAlertWhenTransferRejectedByServer(String caseName, String amount,
                                                                 String expectedMessage) {
        // Предусловия (API)
        UserWithAccount userA = SetupSteps.createUserWithFundedAccount(adminToken,
                SETUP_DEPOSIT_AMOUNT, SETUP_DEPOSIT_COUNT);
        UserWithAccount userB = SetupSteps.createUserWithEmptyAccount(adminToken);
        String recipientAccount = userB.getAccount().getAccountNumber();
        // Предусловия (UI)
        String userAName = getUsername(userA.getToken());
        loginAs(userAName, USER_PASSWORD);
        checkHeader(NONAME_TEXT, userAName);
        // Шаг 1
        openTransferForm();
        // Шаг 2
        String acc1Option = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getAccount().getAccountNumber(), SETUP_BALANCE_UI);
        $(ACCOUNT_SELECT).$$(OPTION).shouldHave(size(ONE_ACCOUNT_OPTIONS_COUNT));
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(CHOOSE_ACCOUNT_OPTION_TEXT)).should(exist);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(acc1Option)).should(exist);
        // Шаг 3
        $(ACCOUNT_SELECT).selectOption(acc1Option);
        $(ACCOUNT_SELECT).getSelectedOption().shouldHave(exactText(acc1Option));
        // Шаг 4
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).setValue(recipientAccount);
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).shouldHave(value(recipientAccount));
        $(ENTER_AMOUNT_INPUT).setValue(amount);
        $(ENTER_AMOUNT_INPUT).shouldHave(value(amount));
        $(CONFIRM_CHECKBOX).click();
        $(CONFIRM_CHECKBOX).shouldBe(checked);
        // Шаг 5
        $$(BUTTON).findBy(text(SEND_TRANSFER_BUTTON_TEXT)).click();
        Alert alert = switchTo().alert();
        assertEquals(expectedMessage, alert.getText());
        // Шаг 6
        alert.accept();
        refresh();
        String acc1OptionAfterAttempt = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getAccount().getAccountNumber(), SETUP_BALANCE_UI);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(acc1OptionAfterAttempt)).should(exist);
    }

    @DisplayName("UI-TR-05. Перевод больше лимита")
    @Test
    public void shouldShowErrorAlertWhenAmountExceedsLimit() {
        // Предусловия (API)
        UserWithAccount userA = SetupSteps.createUserWithEmptyAccount(adminToken);
        SetupSteps.depositToAccount(userA.getToken(), userA.getAccount().getId(),
                DEPOSIT_AT_LIMIT_AMOUNT, DEPOSIT_AT_LIMIT_COUNT);
        SetupSteps.depositToAccount(userA.getToken(), userA.getAccount().getId(),
                SMALL_DEPOSIT_AMOUNT, SMALL_DEPOSIT_COUNT);
        UserWithAccount userB = SetupSteps.createUserWithEmptyAccount(adminToken);
        String recipientAccount = userB.getAccount().getAccountNumber();
        // Предусловия (UI)
        String userAName = getUsername(userA.getToken());
        loginAs(userAName, USER_PASSWORD);
        checkHeader(NONAME_TEXT, userAName);
        // Шаг 1
        openTransferForm();
        // Шаг 2
        String acc1Option = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getAccount().getAccountNumber(), BALANCE_ABOVE_TRANSFER_LIMIT_UI);
        $(ACCOUNT_SELECT).$$(OPTION).shouldHave(size(ONE_ACCOUNT_OPTIONS_COUNT));
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(CHOOSE_ACCOUNT_OPTION_TEXT)).should(exist);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(acc1Option)).should(exist);
        // Шаг 3
        $(ACCOUNT_SELECT).selectOption(acc1Option);
        $(ACCOUNT_SELECT).getSelectedOption().shouldHave(exactText(acc1Option));
        // Шаг 4
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).setValue(recipientAccount);
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).shouldHave(value(recipientAccount));
        $(ENTER_AMOUNT_INPUT).setValue(MAX_TRANSFER_AMOUNT_PLUS_STEP);
        $(ENTER_AMOUNT_INPUT).shouldHave(value(MAX_TRANSFER_AMOUNT_PLUS_STEP));
        $(CONFIRM_CHECKBOX).click();
        $(CONFIRM_CHECKBOX).shouldBe(checked);
        // Шаг 5
        $$(BUTTON).findBy(text(SEND_TRANSFER_BUTTON_TEXT)).click();
        Alert alert = switchTo().alert();
        assertEquals(TRANSFER_ABOVE_LIMIT_MESSAGE, alert.getText());
        // Шаг 6
        alert.accept();
        refresh();
        String acc1OptionAfterAttempt = ACCOUNT_OPTION_TEMPLATE.formatted(
                userA.getAccount().getAccountNumber(), BALANCE_ABOVE_TRANSFER_LIMIT_UI);
        $(ACCOUNT_SELECT).$$(OPTION).findBy(exactText(acc1OptionAfterAttempt)).should(exist);
    }

    // Шаг 1: открыть форму перевода и проверить ее
    private void openTransferForm() {
        $(withText(MAKE_A_TRANSFER_TEXT)).click();
        $$(PAGE_TITLE).findBy(text(MAKE_A_TRANSFER_TEXT)).shouldBe(visible);
        $(withText(NEW_TRANSFER_BUTTON_TEXT)).shouldBe(visible);
        $(withText(TRANSFER_AGAIN_BUTTON_TEXT)).shouldBe(visible);
        $(withText(SELECT_ACCOUNT_TEXT)).shouldBe(visible);
        $(ACCOUNT_SELECT).shouldBe(visible);
        $(withText(RECIPIENT_NAME_TEXT)).shouldBe(visible);
        $(RECIPIENT_NAME_INPUT).shouldBe(visible);
        $(withText(RECIPIENT_ACCOUNT_NUMBER_TEXT)).shouldBe(visible);
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).shouldBe(visible);
        $(withText(AMOUNT_TEXT)).shouldBe(visible);
        $(ENTER_AMOUNT_INPUT).shouldBe(visible);
        $(withText(CONFIRM_DETAILS_TEXT)).shouldBe(visible);
        $(CONFIRM_CHECKBOX).shouldBe(visible).shouldNotBe(checked);
        $$(BUTTON).findBy(text(SEND_TRANSFER_BUTTON_TEXT)).shouldBe(visible);
    }

    // Шаг 4–8: заполнить получателя и сумму, отметить чекбокс, отправить форму, проверить alert и очистку формы
    private void makeTransfer(String recipientAccount, String amount) {
        // Шаг 4
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).setValue(recipientAccount);
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).shouldHave(value(recipientAccount));
        // Шаг 5
        $(ENTER_AMOUNT_INPUT).setValue(amount);
        $(ENTER_AMOUNT_INPUT).shouldHave(value(amount));
        // Шаг 6
        $(CONFIRM_CHECKBOX).click();
        $(CONFIRM_CHECKBOX).shouldBe(checked);
        // Шаг 7
        $$(BUTTON).findBy(text(SEND_TRANSFER_BUTTON_TEXT)).click();
        Alert alert = switchTo().alert();
        assertEquals(TRANSFER_SUCCESS_TEMPLATE.formatted(amount, recipientAccount), alert.getText());
        // Шаг 8
        alert.accept();
        $$(PAGE_TITLE).findBy(text(MAKE_A_TRANSFER_TEXT)).shouldBe(visible);
        $(ACCOUNT_SELECT).getSelectedOption().shouldHave(exactText(CHOOSE_ACCOUNT_OPTION_TEXT));
        $(RECIPIENT_NAME_INPUT).shouldBe(visible).shouldBe(empty);
        $(RECIPIENT_ACCOUNT_NUMBER_INPUT).shouldBe(visible).shouldBe(empty);
        $(ENTER_AMOUNT_INPUT).shouldBe(visible).shouldBe(empty);
        $(CONFIRM_CHECKBOX).shouldBe(visible).shouldNotBe(checked);
    }
}
