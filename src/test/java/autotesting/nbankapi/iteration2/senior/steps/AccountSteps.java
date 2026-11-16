package autotesting.nbankapi.iteration2.senior.steps;

import autotesting.nbankapi.iteration2.senior.models.account.CreateAccountResponseDto;
import autotesting.nbankapi.iteration2.senior.models.account.CustomerAccountResponseDto;
import autotesting.nbankapi.iteration2.senior.models.account.TransactionDto;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.Endpoint;
import autotesting.nbankapi.iteration2.senior.requests.skeleton.requesters.ValidatedCrudRequester;
import autotesting.nbankapi.iteration2.senior.specs.ResponseSpecs;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public class AccountSteps {
    private AccountSteps() {
    }

    public static CreateAccountResponseDto createAccount(RequestSpecification authSpec) {
        return new ValidatedCrudRequester<CreateAccountResponseDto>(
                authSpec, Endpoint.CREATE_ACCOUNT_BY_USER, ResponseSpecs.created()).post();
    }

    public static List<CustomerAccountResponseDto> getAccounts(RequestSpecification authSpec) {
        return new ValidatedCrudRequester<CustomerAccountResponseDto>(
                authSpec, Endpoint.GET_CUSTOMER_ACCOUNT, ResponseSpecs.ok()).getAsList();
    }

    public static List<TransactionDto> getTransactions(RequestSpecification authSpec, int accountId) {
        return new ValidatedCrudRequester<TransactionDto>(
                authSpec,
                Endpoint.GET_TRANSACTION,
                ResponseSpecs.ok()
        ).getAsList("accountId", accountId);
    }
}
