package nbankApi.iteration2.middle.requests.account;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.EmptyModel;
import nbankApi.iteration2.middle.requests.BaseRequest;

import static io.restassured.RestAssured.given;

public class GetAccountTransactionsRequest extends BaseRequest<EmptyModel> {
    private final int accountId;

    public GetAccountTransactionsRequest(RequestSpecification requestSpecification,
                                         ResponseSpecification responseSpecification, int accountId) {
        super(requestSpecification, responseSpecification);
        this.accountId = accountId;
    }

    @Override
    public ValidatableResponse get() {
        return given(requestSpecification)
                .pathParam("accountId", accountId)
                .get("/api/v1/accounts/{accountId}/transactions")
                .then()
                .spec(responseSpecification);
    }
}
