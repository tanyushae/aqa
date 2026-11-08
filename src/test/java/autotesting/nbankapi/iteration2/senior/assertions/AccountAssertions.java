package autotesting.nbankapi.iteration2.senior.assertions;

import autotesting.nbankapi.iteration2.senior.constants.TestData;
import autotesting.nbankapi.iteration2.senior.models.account.CustomerAccountResponseDto;
import org.assertj.core.api.SoftAssertions;

import java.util.List;

import static org.assertj.core.api.Assertions.within;

public class AccountAssertions {
    private AccountAssertions() {
    }

    public static void assertAccountBalance(SoftAssertions softly,
                                            List<CustomerAccountResponseDto> actualAccounts,
                                            int accountId,
                                            double expectedBalance) {
        CustomerAccountResponseDto actualAccount = actualAccounts.stream()
                .filter(account -> account.getId() == accountId)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Account with ID " + accountId + " should exist in customer accounts"));

        softly.assertThat(actualAccount.getBalance())
                .as("Balance for account ID %d", accountId)
                .isCloseTo(expectedBalance, within(TestData.MONEY_TOLERANCE));
    }
}