package nbankApi.iteration2.middle.support.steps;

import io.restassured.response.ValidatableResponse;
import nbankApi.iteration2.middle.generators.TestDataGenerator;
import nbankApi.iteration2.middle.models.admin.AdminCreateUserRequestDto;
import nbankApi.iteration2.middle.models.admin.AdminLoginRequestDto;
import nbankApi.iteration2.middle.models.deposit.DepositRequestDto;
import nbankApi.iteration2.middle.models.profile.UpdateCustomerProfileRequestDto;
import nbankApi.iteration2.middle.enums.UserRole;
import nbankApi.iteration2.middle.models.account.CustomerAccountResponseDto;
import nbankApi.iteration2.middle.requests.admin.AdminCreateUserRequest;
import nbankApi.iteration2.middle.requests.admin.AdminLoginRequest;
import nbankApi.iteration2.middle.requests.user.UserCreateAccountRequest;
import nbankApi.iteration2.middle.requests.deposit.DepositRequest;
import nbankApi.iteration2.middle.requests.profile.GetCustomerProfileRequest;
import nbankApi.iteration2.middle.requests.profile.UpdateCustomerProfileRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;
import nbankApi.iteration2.middle.tests.BaseTest;

import static nbankApi.iteration2.middle.constants.ApiConstants.*;

import static org.hamcrest.Matchers.equalTo;

public class SetupSteps {

    public static String getAdminToken() {
        AdminLoginRequestDto adminDto = new AdminLoginRequestDto("admin", "admin");
        return new AdminLoginRequest(RequestSpecs.unauthSpec(), ResponseSpecs.successOk())
                .post(adminDto)
                .extract()
                .response()
                .header("Authorization");
    }

    public static String createUserAndGetToken(String adminToken) {
        String username = TestDataGenerator.generateUserName("user_");
        AdminCreateUserRequestDto createUserDto = new AdminCreateUserRequestDto(username, VALID_PASSWORD, UserRole.USER);

        ValidatableResponse response = new AdminCreateUserRequest(
                RequestSpecs.authSpec(adminToken),
                ResponseSpecs.successCreated())
                .post(createUserDto);

        String userToken = response.extract().header("Authorization");
        int userId = getCustomerId(userToken);
        BaseTest.trackUser(userId);

        return userToken;
    }

    public static int getCustomerId(String userToken) {
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

    public static CustomerWithAccount createAndFundAccount(String adminToken, double depositAmount) {
        return createAndFundAccountMultipleTimes(adminToken, depositAmount, 1);
    }

    public static CustomerWithTwoAccounts createCustomerWithTwoAccounts(String adminToken, double depositAmount) {
        String userToken = createUserAndGetToken(adminToken);

        CustomerAccountResponseDto account1 = createAccount(userToken);
        if (depositAmount > 0) {
            DepositRequestDto depositDto = new DepositRequestDto(account1.getId(), depositAmount);
            new DepositRequest(RequestSpecs.authSpec(userToken), ResponseSpecs.successOk())
                    .post(depositDto);
        }

        CustomerAccountResponseDto account2 = createAccount(userToken);

        return new CustomerWithTwoAccounts(userToken, account1, account2);
    }

    public static CustomerWithAccount createAndFundAccountMultipleTimes(String adminToken, double depositAmount, int times) {
        String userToken = createUserAndGetToken(adminToken);
        CustomerAccountResponseDto account = createAccount(userToken);

        if (depositAmount > 0 && times > 0) {
            DepositRequestDto depositDto = new DepositRequestDto(account.getId(), depositAmount);
            for (int i = 0; i < times; i++) {
                new DepositRequest(RequestSpecs.authSpec(userToken), ResponseSpecs.successOk())
                        .post(depositDto);
            }
        }
        return new CustomerWithAccount(userToken, account);
    }

    public static String createUserWithValidProfileName(String adminToken) {
        String userToken = createUserAndGetToken(adminToken);
        int userId = getCustomerId(userToken);

        UpdateCustomerProfileRequestDto profileDto = new UpdateCustomerProfileRequestDto(VALID_DEFAULT_PROFILE_NAME);
        new UpdateCustomerProfileRequest(RequestSpecs.authSpec(userToken), ResponseSpecs.successOk())
                .put(profileDto)
                .body("message", equalTo("Profile updated successfully"))
                .body("customer.id", equalTo(userId))
                .body("customer.name", equalTo(VALID_DEFAULT_PROFILE_NAME));
        return userToken;
    }
}
