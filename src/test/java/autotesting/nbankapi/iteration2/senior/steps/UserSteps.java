package autotesting.nbankapi.iteration2.senior.steps;

import autotesting.nbankapi.iteration2.senior.generators.RandomModelGenerator;
import autotesting.nbankapi.iteration2.senior.models.admin.CreateUserRequestDto;
import autotesting.nbankapi.iteration2.senior.models.profile.GetCustomerProfileResponseDto;
import autotesting.nbankapi.iteration2.senior.models.profile.UpdateCustomerProfileRequestDto;
import autotesting.nbankapi.iteration2.senior.models.profile.UpdateCustomerProfileResponseDto;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.Endpoint;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.CrudRequester;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.ValidatedCrudRequester;
import autotesting.nbankapi.iteration2.senior.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.senior.specs.ResponseSpecs;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class UserSteps {
    private UserSteps() {
    }

    public static RequestSpecification createUserAndAuthenticate() {
        CreateUserRequestDto userDto = RandomModelGenerator.generate(CreateUserRequestDto.class);
        AdminSteps.createUser(userDto);
        return RequestSpecs.authAsUser(userDto.getUsername(), userDto.getPassword());
    }

    public static GetCustomerProfileResponseDto getProfile(RequestSpecification userAuthSpec) {
        return new ValidatedCrudRequester<GetCustomerProfileResponseDto>(
                userAuthSpec, Endpoint.GET_CUSTOMER_PROFILE, ResponseSpecs.ok()
        ).get();
    }

    public static int getCustomerId(RequestSpecification userAuthSpec) {
        return getProfile(userAuthSpec).getId();
    }

    public static UpdateCustomerProfileResponseDto updateProfileName(RequestSpecification userAuthSpec, String name) {
        return updateProfile(userAuthSpec, UpdateCustomerProfileRequestDto.builder().name(name).build());
    }

    public static UpdateCustomerProfileResponseDto updateProfile(RequestSpecification userAuthSpec,
                                                                 UpdateCustomerProfileRequestDto dto) {
        return new ValidatedCrudRequester<UpdateCustomerProfileResponseDto>(
                userAuthSpec, Endpoint.UPDATE_CUSTOMER_PROFILE, ResponseSpecs.ok()
        ).update(dto);
    }

    public static ValidatableResponse updateProfileExpectingError(RequestSpecification userAuthSpec,
                                                                  UpdateCustomerProfileRequestDto dto,
                                                                  ResponseSpecification responseSpec) {
        return new CrudRequester(userAuthSpec, Endpoint.UPDATE_CUSTOMER_PROFILE, responseSpec).update(dto);
    }
}
