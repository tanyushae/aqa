package nbankApi.iteration2.middle.requests.profile;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.profile.UpdateCustomerProfileRequestDto;
import nbankApi.iteration2.middle.requests.BaseRequest;

import static io.restassured.RestAssured.given;

public class UpdateCustomerProfileRequest extends BaseRequest<UpdateCustomerProfileRequestDto> {

    public UpdateCustomerProfileRequest(RequestSpecification requestSpecification,
                                        ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse put(UpdateCustomerProfileRequestDto model) {
        return given(requestSpecification)
                .body(model)
                .put("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
