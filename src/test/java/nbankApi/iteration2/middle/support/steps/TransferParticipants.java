package nbankApi.iteration2.middle.support.steps;

/**
 * Участники перевода и их балансы до перевода
 */
public record TransferParticipants(
        String senderToken,
        int senderAccountId,
        double senderInitialBalance,
        String receiverToken,
        int receiverAccountId,
        double receiverInitialBalance
) {
}
