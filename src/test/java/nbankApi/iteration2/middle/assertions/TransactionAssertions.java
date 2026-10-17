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
                                                int accountId, double amount, int relatedAccountId) {

        assertHistoryTransaction(softly, userToken, accountId,
                TransactionType.DEPOSIT, amount, relatedAccountId);
    }

    public static void assertTransactionOut(SoftAssertions softly, String userToken,
                                            int accountId, double amount, int relatedAccountId) {

        assertHistoryTransaction(softly, userToken, accountId,
                TransactionType.TRANSFER_OUT, amount, relatedAccountId);
    }

    public static void assertTransactionIn(SoftAssertions softly, String userToken,
                                           int accountId, double amount, int relatedAccountId) {

        assertHistoryTransaction(softly, userToken, accountId,
                TransactionType.TRANSFER_IN, amount, relatedAccountId);
    }

    private static void assertHistoryTransaction(SoftAssertions softly, String userToken,
                                                 int accountId, TransactionType expectedType, double amount,
                                                 int relatedAccountId) {

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
                            .as("Check transaction amount")
                            .isEqualTo(amount);

                    softly.assertThat(transaction.getRelatedAccountId())
                            .as("Check related account ID")
                            .isEqualTo(relatedAccountId);
                });
    }
}
