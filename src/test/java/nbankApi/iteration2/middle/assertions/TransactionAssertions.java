package nbankApi.iteration2.middle.assertions;

import io.restassured.common.mapper.TypeRef;
import nbankApi.iteration2.middle.enums.TransactionType;
import nbankApi.iteration2.middle.models.account.TransactionDto;
import nbankApi.iteration2.middle.requests.account.GetAccountTransactionsRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;
import org.assertj.core.api.SoftAssertions;

import java.util.List;

public class TransactionAssertions {

    public static void assertDepositTransaction(SoftAssertions softly, String userToken,
                                                int accountId, double expectedAmount, int expectedRelatedAccountId) {

        assertTransactionHistoryContains(softly, userToken, accountId,
                TransactionType.DEPOSIT, expectedAmount, expectedRelatedAccountId);
    }

    public static void assertTransferOutTransaction(SoftAssertions softly, String userToken,
                                            int accountId, double expectedAmount, int expectedRelatedAccountId) {

        assertTransactionHistoryContains(softly, userToken, accountId,
                TransactionType.TRANSFER_OUT, expectedAmount, expectedRelatedAccountId);
    }

    public static void assertTransferInTransaction(SoftAssertions softly, String userToken,
                                           int accountId, double expectedAmount, int expectedRelatedAccountId) {

        assertTransactionHistoryContains(softly, userToken, accountId,
                TransactionType.TRANSFER_IN, expectedAmount, expectedRelatedAccountId);
    }

    private static void assertTransactionHistoryContains(SoftAssertions softly, String userToken,
                                                 int accountId, TransactionType expectedType, double expectedAmount,
                                                 int expectedRelatedAccountId) {

        List<TransactionDto> transactions = new GetAccountTransactionsRequest(
                RequestSpecs.authSpec(userToken),
                ResponseSpecs.successOk(),
                accountId
        )
                .get()
                .extract()
                .as(new TypeRef<List<TransactionDto>>() {
                });

        softly.assertThat(transactions)
                .as("Check that list of transactions contains a matching transaction of type %s", expectedType)
                .anySatisfy(transaction -> {
                    softly.assertThat(transaction.getType())
                            .as("Check transaction type")
                            .isEqualTo(expectedType);

                    softly.assertThat(transaction.getAmount())
                            .as("Check transaction expectedAmount")
                            .isEqualTo(expectedAmount);

                    softly.assertThat(transaction.getRelatedAccountId())
                            .as("Check related account ID")
                            .isEqualTo(expectedRelatedAccountId);
                });
    }
}
