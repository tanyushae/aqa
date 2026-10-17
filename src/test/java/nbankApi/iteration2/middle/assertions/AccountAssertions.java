package nbankApi.iteration2.middle.assertions;

import io.restassured.common.mapper.TypeRef;
import nbankApi.iteration2.middle.models.account.CustomerAccountResponseDto;
import nbankApi.iteration2.middle.requests.account.GetCustomerAccountRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;
import org.assertj.core.api.SoftAssertions;

import java.util.List;

public class AccountAssertions {

    public static void assertAccountBalance(SoftAssertions softly, String userToken,
                                            int accountId, double expectedBalance) {
        List<CustomerAccountResponseDto> accounts = new GetCustomerAccountRequest(
                RequestSpecs.authSpec(userToken),
                ResponseSpecs.successOk()
        )
                .get()
                .extract()
                .as(new TypeRef<List<CustomerAccountResponseDto>>() {
                });

        CustomerAccountResponseDto account = accounts.stream()
                .filter(acc -> acc.getId() == accountId)
                .findFirst()
                .orElse(null);

        softly.assertThat(account)
                .as("Account with ID %s should exist in the list", accountId)
                .isNotNull();

        if (account != null) {
            softly.assertThat(account.getBalance())
                    .as("Check balance for account ID %s", accountId)
                    .isEqualTo(expectedBalance);
        }
    }
}