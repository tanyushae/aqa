package nbankApi.iteration2.middle.requests.user;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.EmptyModel;
import nbankApi.iteration2.middle.requests.BaseRequest;

import static io.restassured.RestAssured.given;

public class UserCreateAccountRequest extends BaseRequest<EmptyModel> {
    public UserCreateAccountRequest(RequestSpecification requestSpecification,
                                    ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(){
        return given(requestSpecification)
                .post("/api/v1/accounts")
                .then()
                .spec(responseSpecification);
    }
}
