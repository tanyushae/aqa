package nbankApi.iteration2.middle.requests.admin;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.admin.AdminCreateUserRequestDto;
import nbankApi.iteration2.middle.requests.BaseRequest;

import static io.restassured.RestAssured.*;

public class AdminCreateUserRequest extends BaseRequest<AdminCreateUserRequestDto> {

    public AdminCreateUserRequest(RequestSpecification requestSpecification,
                                  ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(AdminCreateUserRequestDto model) {
        return given(requestSpecification)
                .body(model)
                .post("/api/v1/admin/users")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}