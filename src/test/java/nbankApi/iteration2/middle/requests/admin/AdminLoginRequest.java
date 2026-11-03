package nbankApi.iteration2.middle.requests.admin;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.admin.AdminLoginRequestDto;
import nbankApi.iteration2.middle.requests.BaseRequest;

import static io.restassured.RestAssured.given;

public class AdminLoginRequest extends BaseRequest<AdminLoginRequestDto> {

    public AdminLoginRequest(RequestSpecification requestSpecification,
                             ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(AdminLoginRequestDto model) {
        return given(requestSpecification)
                .body(model)
                .post("/api/v1/auth/login")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
