package nbankApi.iteration2.middle.assertions;

import io.restassured.response.ValidatableResponse;
import nbankApi.iteration2.middle.enums.TransactionType;
import nbankApi.iteration2.middle.models.deposit.DepositResponseDto;
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

            if (responseDto.getTransactions() != null) {
                softly.assertThat(responseDto.getTransactions())
                        .as("Response should contain a deposit transaction with specified parameters")
                        .anySatisfy(transaction -> {
                            softly.assertThat(transaction.getType()).isEqualTo(TransactionType.DEPOSIT);
                            softly.assertThat(transaction.getAmount()).isEqualTo(expectedDepositAmount);
                            softly.assertThat(transaction.getRelatedAccountId()).isEqualTo(expectedAccountId);
                        });
            }
        }
    }
}
