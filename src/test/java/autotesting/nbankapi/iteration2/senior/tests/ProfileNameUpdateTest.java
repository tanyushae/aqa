package autotesting.nbankapi.iteration2.senior.tests;

import autotesting.nbankapi.iteration2.senior.comparison.ModelAssertions;
import autotesting.nbankapi.iteration2.senior.models.profile.GetCustomerProfileResponseDto;
import autotesting.nbankapi.iteration2.senior.models.profile.UpdateCustomerProfileRequestDto;
import autotesting.nbankapi.iteration2.senior.models.profile.UpdateCustomerProfileResponseDto;
import autotesting.nbankapi.iteration2.senior.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.senior.specs.ResponseSpecs;
import autotesting.nbankapi.iteration2.senior.steps.UserSteps;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.INVALID_PROFILE_NAME_MESSAGE;
import static autotesting.nbankapi.iteration2.senior.constants.ApiMessages.UPDATE_PROFILE_NAME_SUCCESS_MESSAGE;

public class ProfileNameUpdateTest extends BaseTest {

    private static final String VALID_PROFILE_NAME = "New Name";
    private static final String VALID_PROFILE_NAME_WITH_INNER_UPPERCASE = "New ReName";
    private static final String VALID_PROFILE_NAME_CYRILLIC = "Иван Иванов";
    private static final String INVALID_PROFILE_NAME_ONE_WORD_ONLY = "NewName";
    private static final String INVALID_PROFILE_NAME_THREE_WORDS = "New Name Name";
    private static final String INVALID_PROFILE_NAME_AS_EMPTY_STRING = "";
    private static final String INVALID_PROFILE_NAME_WITH_DIGITS = "Lena2 Rect";
    private static final String INVALID_PROFILE_NAME_WITH_HYPHEN = "Ivan-Petrov";

    private RequestSpecification userAuthSpec;
    private int customerId;

    @BeforeEach
    public void setUp() {
        userAuthSpec = UserSteps.createUserAndAuthenticate();
        customerId = UserSteps.getCustomerId(userAuthSpec);
    }

    private static UpdateCustomerProfileRequestDto buildUpdateProfileRequest(String newProfileName) {
        return UpdateCustomerProfileRequestDto.builder()
                .name(newProfileName)
                .build();
    }

    private static Stream<Arguments> validProfileNameUpdateCases() {
        return Stream.of(
                // caseName                                  newProfileName
                Arguments.of("typical: Latin first and last name", VALID_PROFILE_NAME),
                Arguments.of("format: uppercase letter inside word", VALID_PROFILE_NAME_WITH_INNER_UPPERCASE),
                Arguments.of("format: Cyrillic first and last name", VALID_PROFILE_NAME_CYRILLIC)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("validProfileNameUpdateCases")
    public void shouldUpdateWhenValidProfileName(String caseName, String newProfileName) {
        UpdateCustomerProfileRequestDto updateProfileRequest = buildUpdateProfileRequest(newProfileName);
        UpdateCustomerProfileResponseDto updateProfileResponse = UserSteps.updateProfile(userAuthSpec, updateProfileRequest);

        ModelAssertions.assertThatModels(softly, updateProfileRequest, updateProfileResponse).match();
        softly.assertThat(updateProfileResponse.getCustomer().getId()).isEqualTo(customerId);
        softly.assertThat(updateProfileResponse.getMessage()).isEqualTo(UPDATE_PROFILE_NAME_SUCCESS_MESSAGE);

        GetCustomerProfileResponseDto currentProfile = UserSteps.getProfile(userAuthSpec);
        softly.assertThat(currentProfile.getId()).isEqualTo(customerId);
        softly.assertThat(currentProfile.getName()).isEqualTo(newProfileName);
    }

    private static Stream<Arguments> invalidProfileNameUpdateCases() {
        return Stream.of(
                // caseName                                  invalidProfileName
                Arguments.of("400: null name", null),
                Arguments.of("400: name of one word", INVALID_PROFILE_NAME_ONE_WORD_ONLY),
                Arguments.of("400: name of three words", INVALID_PROFILE_NAME_THREE_WORDS),
                Arguments.of("400: empty name", INVALID_PROFILE_NAME_AS_EMPTY_STRING),
                Arguments.of("400: digits in name", INVALID_PROFILE_NAME_WITH_DIGITS),
                Arguments.of("400: hyphen in name", INVALID_PROFILE_NAME_WITH_HYPHEN)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("invalidProfileNameUpdateCases")
    public void shouldReturn400WhenInvalidProfileName(String caseName, String invalidProfileName) {
        UserSteps.updateProfileName(userAuthSpec, VALID_PROFILE_NAME);

        UpdateCustomerProfileRequestDto updateProfileRequest = buildUpdateProfileRequest(invalidProfileName);

        UserSteps.updateProfileExpectingError(userAuthSpec, updateProfileRequest, ResponseSpecs.badRequest(INVALID_PROFILE_NAME_MESSAGE));

        GetCustomerProfileResponseDto currentProfile = UserSteps.getProfile(userAuthSpec);
        softly.assertThat(currentProfile.getId()).isEqualTo(customerId);
        softly.assertThat(currentProfile.getName()).isEqualTo(VALID_PROFILE_NAME);
    }

    @Test
    public void shouldReturn401WhenUnauthorizedUser() {
        UserSteps.updateProfileName(userAuthSpec, VALID_PROFILE_NAME);

        UpdateCustomerProfileRequestDto updateProfileRequest = buildUpdateProfileRequest(VALID_PROFILE_NAME_WITH_INNER_UPPERCASE);

        UserSteps.updateProfileExpectingError(RequestSpecs.unauthSpec(), updateProfileRequest, ResponseSpecs.unauthorized());

        GetCustomerProfileResponseDto currentProfile = UserSteps.getProfile(userAuthSpec);
        softly.assertThat(currentProfile.getId()).isEqualTo(customerId);
        softly.assertThat(currentProfile.getName()).isEqualTo(VALID_PROFILE_NAME);
    }
}
