package nbankApi.iteration2.middle.tests;

import nbankApi.iteration2.middle.support.steps.SetupSteps;
import nbankApi.iteration2.middle.assertions.ProfileAssertions;
import nbankApi.iteration2.middle.models.profile.UpdateCustomerProfileRequestDto;
import nbankApi.iteration2.middle.requests.profile.UpdateCustomerProfileRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static nbankApi.iteration2.middle.constants.ApiConstants.*;

public class ProfileNameUpdateTest extends BaseTest {

    private static final String VALID_PROFILE_NAME = "New Name";
    private static final String VALID_PROFILE_RENAME = "New ReName";
    private static final String VALID_PROFILE_CYRILLIC_NAME = "Иван Иванов";
    private static final String INVALID_PROFILE_NAME_ONE_WORD_ONLY = "NewName";
    private static final String INVALID_PROFILE_NAME_THREE_WORDS = "New Name Name";
    private static final String INVALID_PROFILE_NAME_AS_EMPTY_STRING = "";
    private static final String INVALID_PROFILE_NAME_WITH_DIGITS = "Lena2 Rect";
    private static final String INVALID_PROFILE_NAME_WITH_CHARACTER = "Ivan-Petrov";

    private String userToken;
    private int userId;

    @BeforeEach
    public void setUp() {
        userToken = SetupSteps.createUserAndGetToken(adminToken);
        userId = SetupSteps.getCustomerId(userToken);
    }

    @Test
    public void shouldSuccessfullyUpdateValidProfileName() {
        UpdateCustomerProfileRequestDto profileDto = UpdateCustomerProfileRequestDto.builder()
                .name(VALID_DEFAULT_PROFILE_NAME)
                .build();

        var response = new UpdateCustomerProfileRequest(
                RequestSpecs.authSpec(userToken),
                ResponseSpecs.successOk())
                .put(profileDto);

        ProfileAssertions.assertProfileUpdateResponse(softly, response, userId, VALID_DEFAULT_PROFILE_NAME);
        ProfileAssertions.assertUserProfile(softly, userToken, userId, VALID_DEFAULT_PROFILE_NAME);
    }

    @ParameterizedTest(name = "Case #{index}: successful update to valid name: \"{0}\"")
    @ValueSource(strings = {
            VALID_PROFILE_NAME,
            VALID_PROFILE_RENAME,
            VALID_PROFILE_CYRILLIC_NAME
    })
    public void shouldSuccessfullyUpdateValidProfileName(String newProfileName) {
        UpdateCustomerProfileRequestDto profileDto = UpdateCustomerProfileRequestDto.builder()
                .name(newProfileName)
                .build();

        var response = new UpdateCustomerProfileRequest(
                RequestSpecs.authSpec(userToken),
                ResponseSpecs.successOk())
                .put(profileDto);

        ProfileAssertions.assertProfileUpdateResponse(softly, response, userId, newProfileName);
        ProfileAssertions.assertUserProfile(softly, userToken, userId, newProfileName);
    }

    @ParameterizedTest(name = "Case #{index}: fail to update profile with invalid name: \"{0}\"")
    @NullSource
    @ValueSource(strings = {
            INVALID_PROFILE_NAME_ONE_WORD_ONLY,
            INVALID_PROFILE_NAME_THREE_WORDS,
            INVALID_PROFILE_NAME_AS_EMPTY_STRING,
            INVALID_PROFILE_NAME_WITH_DIGITS,
            INVALID_PROFILE_NAME_WITH_CHARACTER
    })
    public void shouldReturn400WhenInvalidProfileName(String invalidProfileName) {
        userToken = SetupSteps.createUserWithValidProfileName(adminToken);
        userId = SetupSteps.getCustomerId(userToken);

        UpdateCustomerProfileRequestDto invalidDto = UpdateCustomerProfileRequestDto.builder()
                .name(invalidProfileName)
                .build();

        new UpdateCustomerProfileRequest(
                RequestSpecs.authSpec(userToken),
                ResponseSpecs.badRequest())
                .put(invalidDto);

        ProfileAssertions.assertUserProfile(softly, userToken, userId, VALID_DEFAULT_PROFILE_NAME);
    }

    @Test
    public void shouldReturn403WhenUnauthorizedUser() {
        userToken = SetupSteps.createUserWithValidProfileName(adminToken);
        userId = SetupSteps.getCustomerId(userToken);

        UpdateCustomerProfileRequestDto unauthDto = UpdateCustomerProfileRequestDto.builder()
                .name(VALID_PROFILE_NAME)
                .build();

        new UpdateCustomerProfileRequest(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.unauth())
                .put(unauthDto);

        ProfileAssertions.assertUserProfile(softly, userToken, userId, VALID_DEFAULT_PROFILE_NAME);
    }

    @Test
    public void shouldReturn400WhenRequestWithoutNameFieldInBody() {
        userToken = SetupSteps.createUserWithValidProfileName(adminToken);
        userId = SetupSteps.getCustomerId(userToken);

        UpdateCustomerProfileRequestDto emptyDto = UpdateCustomerProfileRequestDto.builder()
                .build();

        new UpdateCustomerProfileRequest(
                RequestSpecs.authSpec(userToken),
                ResponseSpecs.badRequest())
                .put(emptyDto);

        ProfileAssertions.assertUserProfile(softly, userToken, userId, VALID_DEFAULT_PROFILE_NAME);
    }
}
