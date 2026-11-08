package autotesting.nbankapi.iteration2.senior.requests.skeleton;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public abstract class HttpRequester {
    protected final RequestSpecification requestSpecification;
    protected final Endpoint endpoint;
    protected final ResponseSpecification responseSpecification;

    public HttpRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.endpoint = endpoint;
        this.responseSpecification = responseSpecification;
    }
}
