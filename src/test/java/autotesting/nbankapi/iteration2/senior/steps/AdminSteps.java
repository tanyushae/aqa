package autotesting.nbankapi.iteration2.senior.steps;

import autotesting.nbankapi.iteration2.senior.models.admin.CreateUserRequestDto;
import autotesting.nbankapi.iteration2.senior.models.admin.CreateUserResponseDto;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.Endpoint;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.CrudRequester;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.ValidatedCrudRequester;
import autotesting.nbankapi.iteration2.senior.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.senior.specs.ResponseSpecs;

public class AdminSteps {
    private AdminSteps() {
    }

    public static CreateUserResponseDto createUser(CreateUserRequestDto userDto) {
        CreateUserResponseDto createdUser = new ValidatedCrudRequester<CreateUserResponseDto>(
                RequestSpecs.authAsAdmin(),
                Endpoint.CREATE_USER_BY_ADMIN,
                ResponseSpecs.created()
        ).post(userDto);

        CreatedUsersRegistry.registerUser(createdUser.getId());
        return createdUser;
    }

    public static void deleteUser(int userId) {
        new CrudRequester(
                RequestSpecs.authAsAdmin(),
                Endpoint.DELETE_USER_BY_ADMIN,
                ResponseSpecs.ok()
        ).delete("userId", userId);
    }
}
