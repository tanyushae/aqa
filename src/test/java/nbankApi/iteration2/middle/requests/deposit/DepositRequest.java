package nbankApi.iteration2.middle.requests.deposit;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.deposit.DepositRequestDto;
import nbankApi.iteration2.middle.requests.BaseRequest;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class DepositRequest extends BaseRequest<DepositRequestDto> {

    public DepositRequest(RequestSpecification requestSpecification,
                          ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(DepositRequestDto model) {
        return given(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    public ValidatableResponse post(Map<String, Object> model) {
        return given(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    public ValidatableResponse post() {
        return given(requestSpecification)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
