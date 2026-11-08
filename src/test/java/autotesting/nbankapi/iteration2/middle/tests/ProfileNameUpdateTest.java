package autotesting.nbankapi.iteration2.middle.tests;

import autotesting.nbankapi.iteration2.middle.assertions.ProfileAssertions;
import autotesting.nbankapi.iteration2.middle.enums.ExpectedError;
import autotesting.nbankapi.iteration2.middle.enums.RequestAuth;
import autotesting.nbankapi.iteration2.middle.models.profile.UpdateCustomerProfileRequestDto;
import autotesting.nbankapi.iteration2.middle.requests.profile.UpdateCustomerProfileRequest;
import autotesting.nbankapi.iteration2.middle.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.middle.specs.ResponseSpecs;
import autotesting.nbankapi.iteration2.middle.support.steps.SetupSteps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static autotesting.nbankapi.iteration2.middle.constants.ApiConstants.DEFAULT_PROFILE_NAME;
import static autotesting.nbankapi.iteration2.middle.enums.ExpectedError.BAD_REQUEST;
import static autotesting.nbankapi.iteration2.middle.enums.RequestAuth.AUTHORIZED;

public class ProfileNameUpdateTest extends BaseTest {

    private static final String VALID_PROFILE_NAME_LATIN = "New Name";
    private static final String VALID_PROFILE_NAME_WITH_INNER_UPPERCASE = "New ReName";
    private static final String VALID_PROFILE_NAME_CYRILLIC = "Иван Иванов";
    private static final String INVALID_PROFILE_NAME_ONE_WORD = "NewName";
    private static final String INVALID_PROFILE_NAME_THREE_WORDS = "New Name Name";
    private static final String INVALID_PROFILE_NAME_EMPTY = "";
    private static final String INVALID_PROFILE_NAME_WITH_DIGITS = "Lena2 Rect";
    private static final String INVALID_PROFILE_NAME_WITH_HYPHEN = "Ivan-Petrov";

    private String userToken;
    private int userId;

    @BeforeEach
    public void prepareUserWithDefaultProfileName() {
        userToken = SetupSteps.createUserWithDefaultProfileName(adminToken);
        userId = SetupSteps.getUserIdByToken(userToken);
    }

    private static Stream<Arguments> validProfileNameUpdateCases() {
        return Stream.of(
                // caseName                                       validProfileName
                Arguments.of("typical: Latin first and last name", VALID_PROFILE_NAME_LATIN),
                Arguments.of("format: uppercase letter inside word", VALID_PROFILE_NAME_WITH_INNER_UPPERCASE),
                Arguments.of("format: Cyrillic first and last name", VALID_PROFILE_NAME_CYRILLIC)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("validProfileNameUpdateCases")
    public void shouldUpdateProfileNameWhenNameIsValid(String caseName, String validProfileName) {
        UpdateCustomerProfileRequestDto updateProfileDto = UpdateCustomerProfileRequestDto.builder()
                .name(validProfileName)
                .build();

        var updateProfileResponse = new UpdateCustomerProfileRequest(
                RequestSpecs.authSpec(userToken),
                ResponseSpecs.successOk())
                .put(updateProfileDto);

        ProfileAssertions.assertProfileUpdateResponse(softly, updateProfileResponse, userId, validProfileName);
        ProfileAssertions.assertStoredProfile(softly, userToken, userId, validProfileName);
    }

    private static Stream<Arguments> invalidProfileNameUpdateCases() {
        return Stream.of(
                // caseName                        profileName                          requestAuth               expectedError
                Arguments.of("400: null name", null, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: name of one word", INVALID_PROFILE_NAME_ONE_WORD, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: name of three words", INVALID_PROFILE_NAME_THREE_WORDS, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: empty name", INVALID_PROFILE_NAME_EMPTY, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: digits in name", INVALID_PROFILE_NAME_WITH_DIGITS, AUTHORIZED, BAD_REQUEST),
                Arguments.of("400: hyphen in name", INVALID_PROFILE_NAME_WITH_HYPHEN, AUTHORIZED, BAD_REQUEST),
                Arguments.of("401: request without token", VALID_PROFILE_NAME_LATIN, RequestAuth.UNAUTHORIZED, ExpectedError.UNAUTHORIZED)
        );
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("invalidProfileNameUpdateCases")
    public void shouldNotUpdateProfileNameWhenRequestIsInvalid(String caseName, String profileName,
                                                               RequestAuth requestAuth, ExpectedError expectedError) {
        UpdateCustomerProfileRequestDto updateProfileDto = UpdateCustomerProfileRequestDto.builder()
                .name(profileName)
                .build();

        new UpdateCustomerProfileRequest(requestAuth.requestSpec(userToken), expectedError.responseSpec())
                .put(updateProfileDto);

        ProfileAssertions.assertStoredProfile(softly, userToken, userId, DEFAULT_PROFILE_NAME);
    }
}
