package autotesting.nbankapi.iteration2.senior.customers;

import autotesting.nbankapi.iteration2.senior.models.account.CreateAccountResponseDto;
import io.restassured.specification.RequestSpecification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CustomerWithTwoAccounts {
    private final RequestSpecification authSpec;
    private final CreateAccountResponseDto firstAccount;
    private final CreateAccountResponseDto secondAccount;

    public int getFirstAccountId() {
        return firstAccount.getId();
    }

    public int getSecondAccountId() {
        return secondAccount.getId();
    }
}
