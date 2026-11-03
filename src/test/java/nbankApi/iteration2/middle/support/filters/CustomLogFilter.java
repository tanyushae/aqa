package nbankApi.iteration2.middle.support.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class CustomLogFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {

        System.out.println("\n----------------- REQUEST -----------------");
        System.out.println(requestSpec.getMethod() + " " + requestSpec.getURI());

        if (requestSpec.getBody() != null) {
            System.out.println("Body: " + requestSpec.getBody().toString());
        }

        Response response = ctx.next(requestSpec, responseSpec);

        System.out.println("\n----------------- RESPONSE ----------------");
        System.out.println("Status: " + response.getStatusCode());

        String responseBody = response.getBody().asPrettyString();
        if (!responseBody.isEmpty()) {
            System.out.println("Body:\n" + responseBody);
        }

        return response;
    }
}