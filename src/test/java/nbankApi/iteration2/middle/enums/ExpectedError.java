package nbankApi.iteration2.middle.enums;

import io.restassured.specification.ResponseSpecification;
import nbankApi.iteration2.middle.specs.ResponseSpecs;

import java.util.function.Supplier;

/**
 * Ошибка, которую должен вернуть сервер в негативном кейсе
 */
public enum ExpectedError {
    BAD_REQUEST(ResponseSpecs::badRequest),
    FORBIDDEN(ResponseSpecs::forbidden),
    UNAUTHORIZED(ResponseSpecs::unauthorized);

    private final Supplier<ResponseSpecification> responseSpecFactory;

    ExpectedError(Supplier<ResponseSpecification> responseSpecFactory) {
        this.responseSpecFactory = responseSpecFactory;
    }

    public ResponseSpecification responseSpec() {
        return responseSpecFactory.get();
    }
}
