package autotesting.nbankapi.iteration2.senior.steps;

import autotesting.nbankapi.iteration2.senior.models.transfer.AccountTransferRequestDto;
import autotesting.nbankapi.iteration2.senior.models.transfer.AccountTransferResponseDto;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.Endpoint;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.CrudRequester;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.ValidatedCrudRequester;
import autotesting.nbankapi.iteration2.senior.specs.ResponseSpecs;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class TransferSteps {
    private TransferSteps() {
    }

    public static AccountTransferResponseDto transfer(RequestSpecification authSpec,
                                                      AccountTransferRequestDto dto) {
        return new ValidatedCrudRequester<AccountTransferResponseDto>(
                authSpec, Endpoint.ACCOUNT_TRANSFER, ResponseSpecs.ok())
                .post(dto);
    }

    public static ValidatableResponse transferExpectingError(RequestSpecification authSpec,
                                                             AccountTransferRequestDto dto,
                                                             ResponseSpecification responseSpec) {
        return new CrudRequester(authSpec, Endpoint.ACCOUNT_TRANSFER, responseSpec).post(dto);
    }
}
