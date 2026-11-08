package autotesting.nbankapi.iteration2.senior.auth;

import autotesting.nbankapi.iteration2.senior.constants.ApiHeaders;
import autotesting.nbankapi.iteration2.senior.models.user.UserLoginRequestDto;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.Endpoint;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.CrudRequester;
import autotesting.nbankapi.iteration2.senior.specs.RequestSpecs;
import autotesting.nbankapi.iteration2.senior.specs.ResponseSpecs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenStorage {
    private TokenStorage() {
    }

    private static final Map<String, String> tokensByUsername = new ConcurrentHashMap<>();

    public static String getToken(String username, String password) {
        return tokensByUsername.computeIfAbsent(username, u -> fetchToken(u, password));
    }

    private static String fetchToken(String username, String password) {
        var response = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN_USER,
                ResponseSpecs.ok()
        ).post(UserLoginRequestDto.builder()
                .username(username)
                .password(password)
                .build());

        String token = response.extract().header(ApiHeaders.AUTHORIZATION);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Failed to obtain Authorization header for user: " + username);
        }
        return token;
    }
}
