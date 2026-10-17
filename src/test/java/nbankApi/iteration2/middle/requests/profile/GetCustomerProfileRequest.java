package nbankApi.iteration2.middle.requests.profile;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.EmptyModel;
import nbankApi.iteration2.middle.requests.BaseRequest;

import static io.restassured.RestAssured.given;

public class GetCustomerProfileRequest extends BaseRequest<EmptyModel> {

    public GetCustomerProfileRequest(RequestSpecification requestSpecification,
                                     ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return given(requestSpecification)
                .get("/api/v1/customer/profile")
                .then()
                .spec(responseSpecification);
    }
}
