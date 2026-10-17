package nbankApi.iteration2.middle.assertions;

import io.restassured.response.ValidatableResponse;
import nbankApi.iteration2.middle.models.transfer.AccountTransferResponseDto;
import org.assertj.core.api.SoftAssertions;

public class TransferAssertions {

    public static void assertTransferResponse(SoftAssertions softly, ValidatableResponse response,
                                              int expectedSenderId, double expectedAmount, int expectedReceiverId) {

        AccountTransferResponseDto responseDto = response.extract()
                .as(AccountTransferResponseDto.class);

        softly.assertThat(responseDto).isNotNull();

        if (responseDto != null) {
            softly.assertThat(responseDto.getSenderAccountId())
                    .as("Check sender account ID")
                    .isEqualTo(expectedSenderId);

            softly.assertThat(responseDto.getMessage())
                    .as("Check message")
                    .isEqualTo("Transfer successful");

            softly.assertThat(responseDto.getAmount())
                    .as("Check amount")
                    .isEqualTo(expectedAmount);

            softly.assertThat(responseDto.getReceiverAccountId())
                    .as("Check receiver account ID")
                    .isEqualTo(expectedReceiverId);
        }
    }
}
