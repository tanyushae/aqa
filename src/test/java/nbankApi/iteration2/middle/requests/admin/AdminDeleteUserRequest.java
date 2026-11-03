package nbankApi.iteration2.middle.requests.admin;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.EmptyModel;
import nbankApi.iteration2.middle.requests.BaseRequest;

import static io.restassured.RestAssured.given;

public class AdminDeleteUserRequest extends BaseRequest<EmptyModel> {
    private final int userId;

    public AdminDeleteUserRequest(RequestSpecification requestSpecification,
                                  ResponseSpecification responseSpecification,
                                  int userId) {
        super(requestSpecification, responseSpecification);
        this.userId = userId;
    }

    @Override
    public void delete() {
        given(requestSpecification)
                .pathParam("userId", userId)
                .delete("/api/v1/admin/users/{userId}")
                .then()
                .spec(responseSpecification);
    }
}
