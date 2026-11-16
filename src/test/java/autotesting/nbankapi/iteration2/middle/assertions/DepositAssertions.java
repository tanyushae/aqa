package autotesting.nbankapi.iteration2.middle.assertions;

import io.restassured.response.ValidatableResponse;
import autotesting.nbankapi.iteration2.middle.enums.TransactionType;
import autotesting.nbankapi.iteration2.middle.models.deposit.DepositResponseDto;
import org.assertj.core.api.SoftAssertions;

public class DepositAssertions {

    public static void assertDepositResponse(SoftAssertions softly, ValidatableResponse response,
                                             int expectedAccountId, double expectedDepositAmount,
                                             double expectedTotalBalance) {
        DepositResponseDto responseDto = response.extract().as(DepositResponseDto.class);

        softly.assertThat(responseDto)
                .as("Deposit response DTO should not be null")
                .isNotNull();

        if (responseDto != null) {
            softly.assertThat(responseDto.getId())
                    .as("Check account ID in deposit response")
                    .isEqualTo(expectedAccountId);

            softly.assertThat(responseDto.getBalance())
                    .as("Check updated total balance in deposit response")
                    .isEqualTo(expectedTotalBalance);

            softly.assertThat(responseDto.getTransactions())
                    .as("Transactions list in deposit response should not be empty")
                    .isNotEmpty();

            if (responseDto.getTransactions() != null && !responseDto.getTransactions().isEmpty()) {
                var latestTransaction = responseDto.getTransactions()
                        .get(responseDto.getTransactions().size() - 1);

                softly.assertThat(latestTransaction.getType())
                        .as("Latest transaction type should be DEPOSIT")
                        .isEqualTo(TransactionType.DEPOSIT);

                softly.assertThat(latestTransaction.getAmount())
                        .as("Latest transaction amount should match requested deposit amount")
                        .isEqualTo(expectedDepositAmount);

                softly.assertThat(latestTransaction.getRelatedAccountId())
                        .as("Latest transaction account ID should match expected account")
                        .isEqualTo(expectedAccountId);
            }
        }
    }
}
