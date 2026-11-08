package autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters;

import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.Endpoint;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.HttpRequester;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.interfaces.CrudEndpoint;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.List;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequester implements CrudEndpoint<T, BaseModel> {
    private final CrudRequester crudRequester;
    private final Class<T> responseType;

    public ValidatedCrudRequester(RequestSpecification requestSpecification,
                                  Endpoint endpoint,
                                  ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
        this.responseType = responseTypeOf(endpoint);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> responseTypeOf(Endpoint endpoint) {
        Class<?> type = endpoint.getResponseModel();
        if (type == null || type == BaseModel.class) {
            throw new IllegalStateException(
                    "Endpoint " + endpoint + " has no concrete response model; use CrudRequester");
        }
        return (Class<T>) type;
    }

    @Override
    public T post(BaseModel model) {
        return crudRequester.post(model)
                .extract()
                .as(responseType);
    }

    public T post() {
        return crudRequester.post()
                .extract()
                .as(responseType);
    }

    @Override
    public T get() {
        return crudRequester.get()
                .extract()
                .as(responseType);
    }

    @Override
    public T get(String paramName, Object paramValue) {
        return crudRequester.get(paramName, paramValue).extract().as(responseType);
    }

    public List<T> getAsList() {
        return crudRequester.get()
                .extract()
                .jsonPath()
                .getList("", responseType);
    }

    public List<T> getAsList(String paramName, Object paramValue) {
        return crudRequester.get(paramName, paramValue)
                .extract()
                .jsonPath()
                .getList("", responseType);
    }

    @Override
    public T update(BaseModel model) {
        return crudRequester.update(model)
                .extract()
                .as(responseType);
    }

    @Override
    public T delete(String paramName, Object paramValue) {
        return crudRequester.delete(paramName, paramValue)
                .extract()
                .as(responseType);
    }
}
