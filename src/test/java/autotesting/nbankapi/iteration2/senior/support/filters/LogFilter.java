package autotesting.nbankapi.iteration2.senior.support.filters;

import autotesting.nbankapi.iteration2.senior.constants.ApiHeaders;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class LogFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {

        StringBuilder requestLog = new StringBuilder();
        requestLog.append("\n----------------- REQUEST -----------------\n")
                .append(requestSpec.getMethod()).append(" ").append(requestSpec.getURI()).append("\n");

        if (requestSpec.getHeaders() != null && requestSpec.getHeaders().exist()) {
            requestLog.append("Headers:\n");
            requestSpec.getHeaders().forEach(header -> {
                // Маскируем заголовок авторизации ради безопасности
                String headerValue = ApiHeaders.AUTHORIZATION.equalsIgnoreCase(header.getName())
                        ? "********"
                        : header.getValue();
                requestLog.append("  ").append(header.getName()).append(": ").append(headerValue).append("\n");
            });
        }

        if (requestSpec.getBody() != null) {
            requestLog.append("Body:\n").append(requestSpec.getBody().toString()).append("\n");
        }
        System.out.print(requestLog);

        Response response = ctx.next(requestSpec, responseSpec);

        StringBuilder responseLog = new StringBuilder();
        responseLog.append("\n----------------- RESPONSE ----------------\n")
                .append("Status: ").append(response.getStatusCode()).append("\n");

        String responseBody = response.getBody().asPrettyString();
        if (responseBody != null && !responseBody.isBlank()) {
            responseLog.append("Body:\n").append(responseBody).append("\n");
        }
        System.out.print(responseLog);
        return response;
    }
}