package autotesting.nbankapi.iteration2.senior.steps;

import autotesting.nbankapi.iteration2.senior.models.deposit.DepositRequestDto;
import autotesting.nbankapi.iteration2.senior.models.deposit.DepositResponseDto;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.Endpoint;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.CrudRequester;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.ValidatedCrudRequester;
import autotesting.nbankapi.iteration2.senior.specs.ResponseSpecs;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class DepositSteps {
    private DepositSteps() {
    }

    public static DepositResponseDto deposit(RequestSpecification authSpec, int accountId, double amount) {
        return deposit(authSpec, DepositRequestDto.builder()
                .id(accountId)
                .balance(amount)
                .build());
    }

    public static DepositResponseDto deposit(RequestSpecification authSpec, DepositRequestDto dto) {
        return new ValidatedCrudRequester<DepositResponseDto>(
                authSpec, Endpoint.DEPOSIT, ResponseSpecs.ok()
        ).post(dto);
    }

    public static ValidatableResponse depositExpectingError(RequestSpecification authSpec,
                                                            DepositRequestDto dto,
                                                            ResponseSpecification responseSpec) {
        return new CrudRequester(authSpec, Endpoint.DEPOSIT, responseSpec)
                .post(dto);
    }
}
