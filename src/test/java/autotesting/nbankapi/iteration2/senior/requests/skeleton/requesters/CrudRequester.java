package autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters;

import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.Endpoint;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.HttpRequester;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.interfaces.CrudEndpoint;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequester implements CrudEndpoint<ValidatableResponse, BaseModel> {

    public CrudRequester(RequestSpecification requestSpecification,
                         Endpoint endpoint,
                         ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    private ValidatableResponse validate(Response response) {
        return response.then().assertThat().spec(responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        checkModelMatchesEndpoint(model);
        var body = (model == null) ? "" : model;   // Запросы без модели отправляются с пустым телом
        return validate(given()
                .spec(requestSpecification)
                .body(body)
                .post(endpoint.getUrl()));
    }

    public ValidatableResponse post() {
        return validate(given()
                .spec(requestSpecification)
                .post(endpoint.getUrl()));
    }

    @Override
    public ValidatableResponse get() {
        return validate(given()
                .spec(requestSpecification)
                .get(endpoint.getUrl()));
    }

    @Override
    public ValidatableResponse get(String paramName, Object paramValue) {
        return validate(given()
                .spec(requestSpecification)
                .pathParam(paramName, paramValue)
                .get(endpoint.getUrl()));
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        checkModelMatchesEndpoint(model);
        var body = (model == null) ? "" : model;   // Запросы без модели отправляются с пустым телом
        return validate(given()
                .spec(requestSpecification)
                .body(body)
                .put(endpoint.getUrl()));
    }

    @Override
    public ValidatableResponse delete(String paramName, Object paramValue) {
        return validate(given()
                .spec(requestSpecification)
                .pathParam(paramName, paramValue)
                .delete(endpoint.getUrl()));
    }

    private void checkModelMatchesEndpoint(BaseModel model) {
        if (model != null && !endpoint.getRequestModel().isInstance(model)) {
            throw new IllegalArgumentException("Endpoint " + endpoint + " expects "
                    + endpoint.getRequestModel().getSimpleName() + " but got " + model.getClass().getSimpleName());
        }
    }
}