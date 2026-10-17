package nbankApi.iteration2.middle.requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.models.BaseModel;

public abstract class BaseRequest<T extends BaseModel> {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public BaseRequest(RequestSpecification requestSpecification,
                       ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }
    public ValidatableResponse post(T model) {
        // todo
        throw new UnsupportedOperationException("POST method is not supported for this request");
    }

    public ValidatableResponse post() {
        // todo
        throw new UnsupportedOperationException("POST method is not supported for this request");
    }

    public ValidatableResponse get() {
        // todo
        throw new UnsupportedOperationException("POST method is not supported for this request");
    }

    public ValidatableResponse put(T model) {
        // todo
        throw new UnsupportedOperationException("PUT method is not supported for this request");
    }

    public void delete() {
        // todo
        throw new UnsupportedOperationException("PUT method is not supported for this request");
    }
}
