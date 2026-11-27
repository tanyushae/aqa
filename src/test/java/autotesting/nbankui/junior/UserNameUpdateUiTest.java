package autotesting.nbankui.junior;

import autotesting.nbankapi.iteration2.middle.assertions.ProfileAssertions;
import autotesting.nbankapi.iteration2.middle.models.profile.UpdateCustomerProfileRequestDto;
import autotesting.nbankapi.iteration2.middle.requests.profile.UpdateCustomerProfileRequest;
import autotesting.nbankapi.iteration2.middle.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.middle.specs.ResponseSpecs;
import autotesting.nbankapi.iteration2.middle.support.steps.SetupSteps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;

import java.util.stream.Stream;

import static autotesting.nbankapi.iteration2.middle.constants.ApiConstants.USER_PASSWORD;
import static autotesting.nbankui.junior.UiConstants.EDIT_PROFILE_TEXT;
import static autotesting.nbankui.junior.UiConstants.NONAME_TEXT;
import static autotesting.nbankui.junior.UiConstants.USERNAME_HEADER_TEMPLATE;
import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exactValue;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.refresh;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserNameUpdateUiTest extends BaseUiTest {
    private static final String PROFILE_HEADER = ".profile-header";
    private static final String NEW_NAME_INPUT = "input[placeholder='Enter new name']";

    private static final String SAVE_CHANGES_BUTTON_TEXT = "Save Changes";

    private static final String VALID_PROFILE_NAME = "New Name";
    private static final String NO_NAME_IN_API = null;
    private static final String ONE_WORD_NAME = "NewName";
    private static final String EMPTY_NAME = "";

    private static final String SAME_NAME_MESSAGE = "⚠️ New name is the same as the current one.";
    private static final String NAME_UPDATED_MESSAGE = "✅ Name updated successfully!";
    private static final String INVALID_NAME_FORMAT_MESSAGE = "Name must contain two words with letters only";
    private static final String EMPTY_NAME_MESSAGE = "❌ Please enter a valid name.";

    private String userToken;
    private int userId;
    private String username;

    @BeforeEach
    public void setUp() {
        // Предусловия (API)
        userToken = SetupSteps.createUserAndGetToken(adminToken);
        userId = SetupSteps.getUserIdByToken(userToken);
        username = getUsername(userToken);
    }

    private void loginAndCheckHeader(String expectedName) {
        loginAs(username, USER_PASSWORD);
        checkHeader(expectedName, username);
    }

    @DisplayName("UI-NAME-01. Успешное изменение имени")
    @Test
    public void shouldUpdateNameWhenNameIsValid() {
        // Предусловия (UI)
        loginAndCheckHeader(NONAME_TEXT);
        // Шаг 1
        $(PROFILE_HEADER).click();
        $(withText(EDIT_PROFILE_TEXT)).shouldBe(visible);
        $(NEW_NAME_INPUT).shouldBe(visible).shouldBe(empty);
        $$(BUTTON).findBy(text(SAVE_CHANGES_BUTTON_TEXT)).shouldBe(visible);
        // Шаг 2
        $(NEW_NAME_INPUT).setValue(VALID_PROFILE_NAME);
        $(NEW_NAME_INPUT).shouldHave(exactValue(VALID_PROFILE_NAME));
        // Шаг 3
        $$(BUTTON).findBy(text(SAVE_CHANGES_BUTTON_TEXT)).click();
        Alert alert = switchTo().alert();
        assertEquals(NAME_UPDATED_MESSAGE, alert.getText());
        // Шаг 4
        alert.accept();
        $(USER_NAME).shouldHave(exactText(VALID_PROFILE_NAME));
        $(USER_USERNAME).shouldHave(exactText(USERNAME_HEADER_TEMPLATE.formatted(username)));
        // Шаг 5
        refresh();
        $(USER_NAME).shouldHave(exactText(VALID_PROFILE_NAME));
        $(USER_USERNAME).shouldHave(exactText(USERNAME_HEADER_TEMPLATE.formatted(username)));
        // Шаг 6: проверка имени на сервере через API
        ProfileAssertions.assertStoredProfile(softly, userToken, userId, VALID_PROFILE_NAME);
    }

    private static Stream<Arguments> invalidNameCases() {
        return Stream.of(
                // caseName | newName | expectedMessage
                Arguments.of("one word", ONE_WORD_NAME, INVALID_NAME_FORMAT_MESSAGE),
                Arguments.of("empty name", EMPTY_NAME, EMPTY_NAME_MESSAGE)
        );
    }

    @DisplayName("UI-NAME-02. Невалидное имя")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidNameCases")
    public void shouldNotUpdateNameWhenNameIsInvalid(String caseName, String newName, String expectedMessage) {
        // Предусловия (UI)
        loginAndCheckHeader(NONAME_TEXT);
        // Шаг 1
        $(PROFILE_HEADER).click();
        $(withText(EDIT_PROFILE_TEXT)).shouldBe(visible);
        $(NEW_NAME_INPUT).shouldBe(visible).shouldBe(empty);
        $$(BUTTON).findBy(text(SAVE_CHANGES_BUTTON_TEXT)).shouldBe(visible);
        // Шаг 2
        $(NEW_NAME_INPUT).setValue(newName);
        $(NEW_NAME_INPUT).shouldHave(exactValue(newName));
        // Шаг 3
        $$(BUTTON).findBy(text(SAVE_CHANGES_BUTTON_TEXT)).click();
        Alert alert = switchTo().alert();
        assertEquals(expectedMessage, alert.getText());
        // Шаг 4
        alert.accept();
        $(USER_NAME).shouldHave(exactText(NONAME_TEXT));
        $(USER_USERNAME).shouldHave(exactText(USERNAME_HEADER_TEMPLATE.formatted(username)));
        // Шаг 5
        refresh();
        $(USER_NAME).shouldHave(exactText(NONAME_TEXT));
        $(USER_USERNAME).shouldHave(exactText(USERNAME_HEADER_TEMPLATE.formatted(username)));
        // Шаг 6: проверка имени на сервере через API
        ProfileAssertions.assertStoredProfile(softly, userToken, userId, NO_NAME_IN_API);
    }

    @DisplayName("UI-NAME-03. Новое имя совпадает с текущим")
    @Test
    public void shouldNotUpdateNameWhenNameIsSameAsCurrent() {
        // Предусловия (API)
        new UpdateCustomerProfileRequest(RequestSpecs.authSpec(userToken), ResponseSpecs.successOk())
                .put(new UpdateCustomerProfileRequestDto(VALID_PROFILE_NAME));
        // Предусловия (UI)
        loginAndCheckHeader(VALID_PROFILE_NAME);
        // Шаг 1
        $(PROFILE_HEADER).click();
        $(withText(EDIT_PROFILE_TEXT)).shouldBe(visible);
        $(NEW_NAME_INPUT).shouldBe(visible).shouldHave(exactValue(VALID_PROFILE_NAME));
        $$(BUTTON).findBy(text(SAVE_CHANGES_BUTTON_TEXT)).shouldBe(visible);
        // Шаг 2
        $(NEW_NAME_INPUT).setValue(VALID_PROFILE_NAME);
        $(NEW_NAME_INPUT).shouldHave(exactValue(VALID_PROFILE_NAME));
        // Шаг 3
        $$(BUTTON).findBy(text(SAVE_CHANGES_BUTTON_TEXT)).click();
        Alert alert = switchTo().alert();
        assertEquals(SAME_NAME_MESSAGE, alert.getText());
        // Шаг 4
        alert.accept();
        $(USER_NAME).shouldHave(exactText(VALID_PROFILE_NAME));
        $(USER_USERNAME).shouldHave(exactText(USERNAME_HEADER_TEMPLATE.formatted(username)));
        // Шаг 5
        refresh();
        $(USER_NAME).shouldHave(exactText(VALID_PROFILE_NAME));
        $(USER_USERNAME).shouldHave(exactText(USERNAME_HEADER_TEMPLATE.formatted(username)));
        // Шаг 6: проверка имени на сервере через API
        ProfileAssertions.assertStoredProfile(softly, userToken, userId, VALID_PROFILE_NAME);
    }
}
