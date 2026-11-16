package autotesting.nbankapi.iteration2.senior.customers;

import autotesting.nbankapi.iteration2.senior.models.account.CreateAccountResponseDto;
import io.restassured.specification.RequestSpecification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CustomerWithAccount {
    private final RequestSpecification authSpec;
    private final CreateAccountResponseDto account;

    public int getAccountId() {
        return account.getId();
    }
}
