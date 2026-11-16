package autotesting.nbankapi.iteration2.middle.requests.account;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import autotesting.nbankapi.iteration2.middle.models.EmptyModel;
import autotesting.nbankapi.iteration2.middle.requests.BaseRequest;

import static io.restassured.RestAssured.given;

public class GetCustomerAccountsRequest extends BaseRequest<EmptyModel> {

    public GetCustomerAccountsRequest(RequestSpecification requestSpecification,
                                     ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return given(requestSpecification)
                .get("/api/v1/customer/accounts")
                .then()
                .spec(responseSpecification);
    }
}