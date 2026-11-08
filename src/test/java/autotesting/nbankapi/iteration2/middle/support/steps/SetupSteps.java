package autotesting.nbankapi.iteration2.middle.support.steps;

import io.restassured.response.ValidatableResponse;
import autotesting.nbankapi.iteration2.middle.enums.UserRole;
import autotesting.nbankapi.iteration2.middle.generators.TestDataGenerator;
import autotesting.nbankapi.iteration2.middle.models.account.CustomerAccountResponseDto;
import autotesting.nbankapi.iteration2.middle.models.admin.AdminCreateUserRequestDto;
import autotesting.nbankapi.iteration2.middle.models.admin.AdminLoginRequestDto;
import autotesting.nbankapi.iteration2.middle.models.deposit.DepositRequestDto;
import autotesting.nbankapi.iteration2.middle.models.profile.UpdateCustomerProfileRequestDto;
import autotesting.nbankapi.iteration2.middle.requests.admin.AdminCreateUserRequest;
import autotesting.nbankapi.iteration2.middle.requests.admin.AdminLoginRequest;
import autotesting.nbankapi.iteration2.middle.requests.deposit.DepositRequest;
import autotesting.nbankapi.iteration2.middle.requests.profile.GetCustomerProfileRequest;
import autotesting.nbankapi.iteration2.middle.requests.profile.UpdateCustomerProfileRequest;
import autotesting.nbankapi.iteration2.middle.requests.user.UserCreateAccountRequest;
import autotesting.nbankapi.iteration2.middle.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.middle.specs.ResponseSpecs;
import autotesting.nbankapi.iteration2.middle.tests.BaseTest;

import static autotesting.nbankapi.iteration2.middle.constants.ApiConstants.*;
import static org.hamcrest.Matchers.equalTo;

public class SetupSteps {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String USERNAME_PREFIX = "user_";

    public static String getAdminToken() {
        AdminLoginRequestDto adminLoginDto = new AdminLoginRequestDto(ADMIN_USERNAME, ADMIN_PASSWORD);
        return new AdminLoginRequest(RequestSpecs.unauthSpec(), ResponseSpecs.successOk())
                .post(adminLoginDto)
                .extract()
                .response()
                .header("Authorization");
    }

    public static String createUserAndGetToken(String adminToken) {
        String username = TestDataGenerator.generateUniqueUsername(USERNAME_PREFIX);
        AdminCreateUserRequestDto createUserDto = new AdminCreateUserRequestDto(username, USER_PASSWORD, UserRole.USER);

        ValidatableResponse createUserResponse = new AdminCreateUserRequest(
                RequestSpecs.authSpec(adminToken),
                ResponseSpecs.successCreated())
                .post(createUserDto);

        String userToken = createUserResponse.extract().header("Authorization");
        BaseTest.registerUserForCleanup(getUserIdByToken(userToken));
        return userToken;
    }

    public static int getUserIdByToken(String userToken) {
        return new GetCustomerProfileRequest(RequestSpecs.authSpec(userToken), ResponseSpecs.successOk())
                .get()
                .extract()
                .path("id");
    }

    public static CustomerAccountResponseDto createAccount(String userToken) {
        return new UserCreateAccountRequest(
                RequestSpecs.authSpec(userToken),
                ResponseSpecs.successCreated())
                .post()
                .extract()
                .as(CustomerAccountResponseDto.class);
    }

    public static void depositToAccount(String userToken, int accountId, double depositAmount, int depositsCount) {
        DepositRequestDto depositDto = new DepositRequestDto(accountId, depositAmount);
        for (int depositNumber = 0; depositNumber < depositsCount; depositNumber++) {
            new DepositRequest(RequestSpecs.authSpec(userToken), ResponseSpecs.successOk())
                    .post(depositDto);
        }
    }

    public static UserWithAccount createUserWithEmptyAccount(String adminToken) {
        String userToken = createUserAndGetToken(adminToken);
        return new UserWithAccount(userToken, createAccount(userToken));
    }

    public static UserWithAccount createUserWithFundedAccount(String adminToken, double depositAmount, int depositsCount) {
        UserWithAccount user = createUserWithEmptyAccount(adminToken);
        depositToAccount(user.getToken(), user.getAccount().getId(), depositAmount, depositsCount);
        return user;
    }

    public static UserWithTwoAccounts createUserWithTwoAccounts(String adminToken,
                                                                double firstAccountDepositAmount,
                                                                int firstAccountDepositsCount) {
        UserWithAccount user = createUserWithFundedAccount(adminToken, firstAccountDepositAmount, firstAccountDepositsCount);
        CustomerAccountResponseDto secondAccount = createAccount(user.getToken());
        return new UserWithTwoAccounts(user.getToken(), user.getAccount(), secondAccount);
    }

    public static TransferParticipants prepareTransferToAnotherUser(String adminToken) {
        UserWithAccount sender = createUserWithFundedAccount(adminToken, SETUP_DEPOSIT_AMOUNT, SETUP_DEPOSITS_COUNT);
        UserWithAccount receiver = createUserWithEmptyAccount(adminToken);
        return new TransferParticipants(
                sender.getToken(), sender.getAccount().getId(), SETUP_DEPOSIT_AMOUNT * SETUP_DEPOSITS_COUNT,
                receiver.getToken(), receiver.getAccount().getId(), NEW_ACCOUNT_BALANCE);
    }

    public static TransferParticipants prepareTransferBetweenOwnAccounts(String adminToken) {
        UserWithTwoAccounts user = createUserWithTwoAccounts(adminToken, SETUP_DEPOSIT_AMOUNT, SETUP_DEPOSITS_COUNT);
        return new TransferParticipants(
                user.getToken(), user.getFirstAccount().getId(), SETUP_DEPOSIT_AMOUNT * SETUP_DEPOSITS_COUNT,
                user.getToken(), user.getSecondAccount().getId(), NEW_ACCOUNT_BALANCE);
    }

    public static String createUserWithDefaultProfileName(String adminToken) {
        String userToken = createUserAndGetToken(adminToken);
        int userId = getUserIdByToken(userToken);

        UpdateCustomerProfileRequestDto updateProfileDto = new UpdateCustomerProfileRequestDto(DEFAULT_PROFILE_NAME);
        new UpdateCustomerProfileRequest(RequestSpecs.authSpec(userToken), ResponseSpecs.successOk())
                .put(updateProfileDto)
                .body("message", equalTo("Profile updated successfully"))
                .body("customer.id", equalTo(userId))
                .body("customer.name", equalTo(DEFAULT_PROFILE_NAME));
        return userToken;
    }
}
