package autotesting.nbankapi.iteration2.senior.specs;

import autotesting.configs.Config;
import autotesting.nbankapi.iteration2.senior.auth.TokenStorage;
import autotesting.nbankapi.iteration2.senior.constants.ApiHeaders;
import autotesting.nbankapi.iteration2.senior.support.filters.LogFilter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.specification.RequestSpecification;

public class RequestSpecs {
    private RequestSpecs() {}

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setConfig(RestAssuredConfig.config().jsonConfig(
                        JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE)))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new LogFilter())
                .setBaseUri(Config.getProperty("server") + Config.getProperty("apiVersion"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification authAsAdmin() {
        return authAsUser(
                Config.getProperty("admin.username"),
                Config.getProperty("admin.password")
        );
    }

    public static RequestSpecification authAsUser(String username, String password) {
        return authWithToken(TokenStorage.getToken(username, password));
    }

    public static RequestSpecification authWithToken(String token) {
        return defaultRequestBuilder()
                .addHeader(ApiHeaders.AUTHORIZATION, token)
                .build();
    }
}
