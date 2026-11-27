package autotesting.nbankui.junior;

import autotesting.configs.Config;
import autotesting.nbankapi.iteration2.middle.models.profile.CustomerResponseDto;
import autotesting.nbankapi.iteration2.middle.requests.profile.GetCustomerProfileRequest;
import autotesting.nbankapi.iteration2.middle.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.middle.specs.ResponseSpecs;
import autotesting.nbankapi.iteration2.middle.tests.BaseTest;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

import static autotesting.nbankui.junior.UiConstants.LOGIN_BUTTON_TEXT;
import static autotesting.nbankui.junior.UiConstants.LOGIN_PAGE_PATH;
import static autotesting.nbankui.junior.UiConstants.USER_DASHBOARD_TEXT;
import static autotesting.nbankui.junior.UiConstants.USERNAME_HEADER_TEMPLATE;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class BaseUiTest extends BaseTest {
    protected static final String USERNAME_INPUT = "input[placeholder='Username']";
    protected static final String PASSWORD_INPUT = "input[placeholder='Password']";
    protected static final String BUTTON = "button";
    protected static final String PAGE_TITLE = "h1";

    protected static final String USER_NAME = ".user-name";
    protected static final String USER_USERNAME = ".user-username";

    protected static final String OPTION = "option";
    protected static final String ACCOUNT_SELECT = ".account-selector";
    protected static final String ENTER_AMOUNT_INPUT = "input[placeholder='Enter amount']";

    protected static final String CHOOSE_ACCOUNT_OPTION_TEXT = "-- Choose an account --";

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getProperty("ui.remote");
        Configuration.baseUrl = Config.getProperty("ui.baseUrl");
        Configuration.browser = Config.getProperty("ui.browser");
        Configuration.browserSize = Config.getProperty("ui.browserSize");
        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));
    }

    @AfterEach
    public void closeBrowser() {
        Selenide.closeWebDriver();
    }

    protected String getUsername(String userToken) {
        return new GetCustomerProfileRequest(RequestSpecs.authSpec(userToken), ResponseSpecs.successOk())
                .get()
                .extract()
                .as(CustomerResponseDto.class)
                .getUsername();
    }

    protected void checkHeader(String expectedName, String username) {
        $(USER_NAME).shouldHave(exactText(expectedName));
        $(USER_USERNAME).shouldHave(exactText(USERNAME_HEADER_TEMPLATE.formatted(username)));
    }

    protected void loginAs(String username, String password) {
        open(LOGIN_PAGE_PATH);
        $(USERNAME_INPUT).setValue(username);
        $(PASSWORD_INPUT).setValue(password);
        $$(BUTTON).findBy(exactText(LOGIN_BUTTON_TEXT)).click();
        $(withText(USER_DASHBOARD_TEXT)).shouldBe(visible);
    }
}
