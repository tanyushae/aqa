package autotesting.nbankui.junior;

import autotesting.nbankapi.iteration2.middle.assertions.AccountAssertions;
import autotesting.nbankapi.iteration2.middle.support.steps.SetupSteps;
import autotesting.nbankapi.iteration2.middle.support.steps.UserWithAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;

import java.util.stream.Stream;

import static autotesting.nbankapi.iteration2.middle.constants.ApiConstants.NEW_ACCOUNT_BALANCE;
import static autotesting.nbankapi.iteration2.middle.constants.ApiConstants.USER_PASSWORD;
import static autotesting.nbankui.junior.UiConstants.ACCOUNT_OPTION_TEMPLATE;
import static autotesting.nbankui.junior.UiConstants.DEPOSIT_MONEY_TEXT;
import static autotesting.nbankui.junior.UiConstants.NONAME_TEXT;
import static autotesting.nbankui.junior.UiConstants.USER_DASHBOARD_TEXT;
import static autotesting.nbankui.junior.UiConstants.ZERO_BALANCE_UI;
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

public class UserDepositUiTest extends BaseUiTest {
    private static final String SELECT_ACCOUNT_TEXT = "Select Account:";
    private static final String ENTER_AMOUNT_TEXT = "Enter Amount:";
    private static final String DEPOSIT_BUTTON_TEXT = "Deposit";

    // Суммы: строки, как их вводят в поле и как они показываются в сообщении
    private static final String REGULAR_DEPOSIT_AMOUNT = "100";
    private static final String FLOAT_DEPOSIT_AMOUNT = "12.34";
    private static final String MAX_DEPOSIT_AMOUNT_PLUS_STEP = "5000.01";
    private static final String ZERO_DEPOSIT_AMOUNT = "0";

    // Баланс в списке счетов после депозита (всегда с двумя знаками)
    private static final String REGULAR_DEPOSIT_BALANCE_UI = "100.00";
    private static final String FLOAT_DEPOSIT_BALANCE_UI = "12.34";

    // Сообщения об успехе: %s заменяется номером счета при вызове .formatted(accountNumber)
    private static final String REGULAR_DEPOSIT_MESSAGE_TEMPLATE = "✅ Successfully deposited $100 to account %s!";
    private static final String FLOAT_DEPOSIT_MESSAGE_TEMPLATE = "✅ Successfully deposited $12.34 to account %s!";

    private static final String DEPOSIT_ABOVE_LIMIT_MESSAGE = "❌ Please deposit less or equal to 5000$.";
    private static final String DEPOSIT_INVALID_AMOUNT_MESSAGE = "❌ Please enter a valid amount.";

    private UserWithAccount user;
    private String accountNumber;

    @BeforeEach
    public void setUp() {
        // Предусловия (API)
        user = SetupSteps.createUserWithEmptyAccount(adminToken);
        accountNumber = user.getAccount().getAccountNumber();
        String username = getUsername(user.getToken());
        // Предусловия (UI)
        loginAs(username, USER_PASSWORD);
        checkHeader(NONAME_TEXT, username);
    }

    private static Stream<Arguments> validDepositCases() {
        return Stream.of(
                // caseName | amount | expectedMessageTemplate | expectedBalanceUi
                Arguments.of("regular amount", REGULAR_DEPOSIT_AMOUNT,
                        REGULAR_DEPOSIT_MESSAGE_TEMPLATE, REGULAR_DEPOSIT_BALANCE_UI),
                Arguments.of("float amount", FLOAT_DEPOSIT_AMOUNT,
                        FLOAT_DEPOSIT_MESSAGE_TEMPLATE, FLOAT_DEPOSIT_BALANCE_UI)
        );
    }

    @DisplayName("UI-DEP-01. Успешный депозит")
    @ParameterizedTest(name = "{0}")
    @MethodSource("validDepositCases")
    public void shouldDepositWhenAmountIsValid(String caseName, String amount,
                                               String expectedMessageTemplate, String expectedBalanceUi) {
        // Шаг 1
        String acc1Option = ACCOUNT_OPTION_TEMPLATE.formatted(accountNumber, ZERO_BALANCE_UI);
        $(withText(DEPOSIT_MONEY_TEXT)).click();
        $$(PAGE_TITLE).findBy(text(DEPOSIT_MONEY_TEXT)).shouldBe(visible);
        $(withText(SELECT_ACCOUNT_TEXT)).shouldBe(visible);
        $(ACCOUNT_SELECT).shouldBe(visible);
        $(ACCOUNT_SELECT).$$(OPTION)
                .findBy(exactText(CHOOSE_ACCOUNT_OPTION_TEXT))
                .should(exist);
        $(ACCOUNT_SELECT).$$(OPTION)
                .findBy(exactText(acc1Option))
                .should(exist);
        $(withText(ENTER_AMOUNT_TEXT)).shouldBe(visible);
        $(ENTER_AMOUNT_INPUT).shouldBe(visible);
        $$(BUTTON).findBy(text(DEPOSIT_BUTTON_TEXT)).shouldBe(visible);
        // Шаг 2
        $(ACCOUNT_SELECT).selectOption(acc1Option);
        $(ACCOUNT_SELECT).getSelectedOption().shouldHave(exactText(acc1Option));
        // Шаг 3
        $(ENTER_AMOUNT_INPUT).setValue(amount);
        $(ENTER_AMOUNT_INPUT).shouldHave(value(amount));
        // Шаг 4
        $$(BUTTON).findBy(text(DEPOSIT_BUTTON_TEXT)).click();
        Alert alert = switchTo().alert();
        assertEquals(expectedMessageTemplate.formatted(accountNumber), alert.getText());
        // Шаг 5
        alert.accept();
        $(withText(USER_DASHBOARD_TEXT)).shouldBe(visible);
        // Шаг 6
        $(withText(DEPOSIT_MONEY_TEXT)).click();
        $(ACCOUNT_SELECT).$$(OPTION)
                .findBy(exactText(ACCOUNT_OPTION_TEMPLATE.formatted(accountNumber, expectedBalanceUi)))
                .should(exist);
        // Шаг 7: проверка баланса на сервере через API
        AccountAssertions.assertAccountBalance(softly, user.getToken(),
                user.getAccount().getId(), Double.parseDouble(amount));
    }

    private static Stream<Arguments> invalidDepositCases() {
        return Stream.of(
                // caseName | amount | expectedMessage
                Arguments.of("max amount plus 0.01", MAX_DEPOSIT_AMOUNT_PLUS_STEP, DEPOSIT_ABOVE_LIMIT_MESSAGE),
                Arguments.of("zero amount", ZERO_DEPOSIT_AMOUNT, DEPOSIT_INVALID_AMOUNT_MESSAGE)
        );
    }

    @DisplayName("UI-DEP-02. Депозит некорректной суммы")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDepositCases")
    public void shouldNotDepositWhenAmountIsInvalid(String caseName, String amount, String expectedMessage) {
        // Шаг 1
        String acc1Option = ACCOUNT_OPTION_TEMPLATE.formatted(accountNumber, ZERO_BALANCE_UI);
        $(withText(DEPOSIT_MONEY_TEXT)).click();
        $$(PAGE_TITLE).findBy(text(DEPOSIT_MONEY_TEXT)).shouldBe(visible);
        $(withText(SELECT_ACCOUNT_TEXT)).shouldBe(visible);
        $(ACCOUNT_SELECT).shouldBe(visible);
        $(ACCOUNT_SELECT).$$(OPTION)
                .findBy(exactText(CHOOSE_ACCOUNT_OPTION_TEXT))
                .should(exist);
        $(ACCOUNT_SELECT).$$(OPTION)
                .findBy(exactText(acc1Option))
                .should(exist);
        $(withText(ENTER_AMOUNT_TEXT)).shouldBe(visible);
        $(ENTER_AMOUNT_INPUT).shouldBe(visible);
        $$(BUTTON).findBy(text(DEPOSIT_BUTTON_TEXT)).shouldBe(visible);
        // Шаг 2
        $(ACCOUNT_SELECT).selectOption(acc1Option);
        $(ACCOUNT_SELECT).getSelectedOption().shouldHave(exactText(acc1Option));
        // Шаг 3
        $(ENTER_AMOUNT_INPUT).setValue(amount);
        $(ENTER_AMOUNT_INPUT).shouldHave(value(amount));
        // Шаг 4
        $$(BUTTON).findBy(text(DEPOSIT_BUTTON_TEXT)).click();
        Alert alert = switchTo().alert();
        assertEquals(expectedMessage, alert.getText());
        // Шаг 5
        alert.accept();
        $$(PAGE_TITLE).findBy(text(DEPOSIT_MONEY_TEXT)).shouldBe(visible);
        // Шаг 6
        refresh();
        $(ACCOUNT_SELECT).$$(OPTION)
                .findBy(exactText(ACCOUNT_OPTION_TEMPLATE.formatted(accountNumber, ZERO_BALANCE_UI)))
                .should(exist);
        // Шаг 7: проверка баланса на сервере через API
        AccountAssertions.assertAccountBalance(softly, user.getToken(),
                user.getAccount().getId(), NEW_ACCOUNT_BALANCE);
    }

    @DisplayName("UI-DEP-03. Двойной клик по кнопке Deposit")
    @Test
    public void shouldDepositOnceWhenDoubleClickButton() {
        // Шаг 1
        String acc1Option = ACCOUNT_OPTION_TEMPLATE.formatted(accountNumber, ZERO_BALANCE_UI);
        $(withText(DEPOSIT_MONEY_TEXT)).click();
        $$(PAGE_TITLE).findBy(text(DEPOSIT_MONEY_TEXT)).shouldBe(visible);
        $(withText(SELECT_ACCOUNT_TEXT)).shouldBe(visible);
        $(ACCOUNT_SELECT).shouldBe(visible);
        $(ACCOUNT_SELECT).$$(OPTION)
                .findBy(exactText(CHOOSE_ACCOUNT_OPTION_TEXT))
                .should(exist);
        $(ACCOUNT_SELECT).$$(OPTION)
                .findBy(exactText(acc1Option))
                .should(exist);
        $(withText(ENTER_AMOUNT_TEXT)).shouldBe(visible);
        $(ENTER_AMOUNT_INPUT).shouldBe(visible);
        $$(BUTTON).findBy(text(DEPOSIT_BUTTON_TEXT)).shouldBe(visible);
        // Шаг 2
        $(ACCOUNT_SELECT).selectOption(acc1Option);
        $(ACCOUNT_SELECT).getSelectedOption().shouldHave(exactText(acc1Option));
        // Шаг 3
        $(ENTER_AMOUNT_INPUT).setValue(REGULAR_DEPOSIT_AMOUNT);
        $(ENTER_AMOUNT_INPUT).shouldHave(value(REGULAR_DEPOSIT_AMOUNT));
        // Шаг 4
        $$(BUTTON).findBy(text(DEPOSIT_BUTTON_TEXT)).doubleClick();
        Alert alert = switchTo().alert();
        assertEquals(REGULAR_DEPOSIT_MESSAGE_TEMPLATE.formatted(accountNumber), alert.getText());
        // Шаг 5
        alert.accept();
        $(withText(USER_DASHBOARD_TEXT)).shouldBe(visible);
        // Шаг 6
        $(withText(DEPOSIT_MONEY_TEXT)).click();
        $(ACCOUNT_SELECT).$$(OPTION)
                .findBy(exactText(ACCOUNT_OPTION_TEMPLATE.formatted(accountNumber, REGULAR_DEPOSIT_BALANCE_UI)))
                .should(exist);
        // Шаг 7: проверка баланса на сервере через API
        AccountAssertions.assertAccountBalance(softly, user.getToken(),
                user.getAccount().getId(), Double.parseDouble(REGULAR_DEPOSIT_BALANCE_UI));
    }
}
