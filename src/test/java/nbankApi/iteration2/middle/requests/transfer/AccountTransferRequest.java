package nbankApi.iteration2.middle.requests.transfer;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.transfer.AccountTransferRequestDto;
import nbankApi.iteration2.middle.requests.BaseRequest;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class AccountTransferRequest extends BaseRequest<AccountTransferRequestDto> {

    public AccountTransferRequest(RequestSpecification requestSpecification,
                                  ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(AccountTransferRequestDto model) {
        return given(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    public ValidatableResponse post(Map<String, Object> model) {
        return given(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    public ValidatableResponse post() {
        return given(requestSpecification)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
