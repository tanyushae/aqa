package autotesting.nbankui.junior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static autotesting.nbankui.junior.UiConstants.DEPOSIT_MONEY_TEXT;
import static autotesting.nbankui.junior.UiConstants.EDIT_PROFILE_TEXT;
import static autotesting.nbankui.junior.UiConstants.LOGIN_BUTTON_TEXT;
import static autotesting.nbankui.junior.UiConstants.MAKE_A_TRANSFER_TEXT;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

public class UnauthorizedAccessUiTest extends BaseUiTest {
    private static final String DEPOSIT_PAGE_PATH = "/deposit";
    private static final String EDIT_PROFILE_PAGE_PATH = "/edit-profile";
    private static final String TRANSFER_PAGE_PATH = "/transfer";

    private static Stream<Arguments> protectedPagesCases() {
        return Stream.of(
                // caseName | pagePath | pageText
                Arguments.of("deposit page", DEPOSIT_PAGE_PATH, DEPOSIT_MONEY_TEXT),
                Arguments.of("edit profile page", EDIT_PROFILE_PAGE_PATH, EDIT_PROFILE_TEXT),
                Arguments.of("transfer page", TRANSFER_PAGE_PATH, MAKE_A_TRANSFER_TEXT)
        );
    }

    @DisplayName("UI-AUTH-01. Доступ к страницам без входа")
    @ParameterizedTest(name = "{0}")
    @MethodSource("protectedPagesCases")
    public void shouldRedirectToLoginPageWhenUserIsNotLoggedIn(String caseName, String pagePath, String pageText) {
        // Шаг 1
        open(pagePath);
        webdriver().shouldNotHave(urlContaining(pagePath));
        $(USERNAME_INPUT).shouldBe(visible);
        $(PASSWORD_INPUT).shouldBe(visible);
        $$(BUTTON).findBy(exactText(LOGIN_BUTTON_TEXT)).shouldBe(visible);
        $(withText(pageText)).shouldNot(exist);
    }
}
