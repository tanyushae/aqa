package nbankApi.iteration2.middle.enums;

import io.restassured.specification.RequestSpecification;
import nbankApi.iteration2.middle.specs.RequestSpecs;

import java.util.function.Function;

/**
 * Как авторизовать запрос: с токеном пользователя или без него
 */
public enum RequestAuth {
    AUTHORIZED(RequestSpecs::authSpec),
    UNAUTHORIZED(userToken -> RequestSpecs.unauthSpec());

    private final Function<String, RequestSpecification> requestSpecFactory;

    RequestAuth(Function<String, RequestSpecification> requestSpecFactory) {
        this.requestSpecFactory = requestSpecFactory;
    }

    public RequestSpecification requestSpec(String userToken) {
        return requestSpecFactory.apply(userToken);
    }
}
