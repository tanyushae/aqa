package autotesting.nbankapi.iteration2.senior.assertions;

import autotesting.nbankapi.iteration2.senior.constants.TestData;
import autotesting.nbankapi.iteration2.senior.enums.TransactionType;
import autotesting.nbankapi.iteration2.senior.models.account.TransactionDto;
import org.assertj.core.api.SoftAssertions;

import java.util.List;

import static org.assertj.core.api.Assertions.within;

public class TransactionAssertions {
    private TransactionAssertions() {
    }

    public static void assertDepositTransaction(SoftAssertions softly,
                                                List<TransactionDto> actualTransactions,
                                                int relatedAccountId, double expectedAmount) {
        assertTransactionOfType(softly, actualTransactions, TransactionType.DEPOSIT, relatedAccountId, expectedAmount);
    }

    public static void assertTransferOutTransaction(SoftAssertions softly,
                                                    List<TransactionDto> actualTransactions,
                                                    int relatedAccountId, double expectedAmount) {
        assertTransactionOfType(softly, actualTransactions, TransactionType.TRANSFER_OUT, relatedAccountId, expectedAmount);
    }

    public static void assertTransferInTransaction(SoftAssertions softly,
                                                   List<TransactionDto> actualTransactions,
                                                   int relatedAccountId, double expectedAmount) {
        assertTransactionOfType(softly, actualTransactions, TransactionType.TRANSFER_IN, relatedAccountId, expectedAmount);
    }

    private static void assertTransactionOfType(SoftAssertions softly,
                                                List<TransactionDto> actualTransactions,
                                                TransactionType expectedType, int relatedAccountId,
                                                double expectedAmount) {
        TransactionDto actualTransaction = actualTransactions.stream()
                .filter(transaction -> transaction.getType() == expectedType)
                .filter(transaction -> transaction.getRelatedAccountId() == relatedAccountId)
                .findFirst()
                .orElseThrow(() -> new AssertionError(String.format(
                        "Transaction %s related to account %d should exist",
                        expectedType, relatedAccountId)));

        softly.assertThat(actualTransaction.getAmount())
                .as("Transaction amount for type %s", expectedType)
                .isCloseTo(expectedAmount, within(TestData.MONEY_TOLERANCE));
    }
}
