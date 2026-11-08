package autotesting.nbankapi.iteration2.middle.enums;

import autotesting.nbankapi.iteration2.middle.support.steps.SetupSteps;
import autotesting.nbankapi.iteration2.middle.support.steps.TransferParticipants;

import java.util.function.Function;

/**
 * Направление перевода: другому пользователю или между своими счетами
 * Каждое значение знает, каким шагом подготовить отправителя и получателя
 */
public enum TransferDirection {
    TO_ANOTHER_USER(SetupSteps::prepareTransferToAnotherUser),
    BETWEEN_OWN_ACCOUNTS(SetupSteps::prepareTransferBetweenOwnAccounts);

    private final Function<String, TransferParticipants> participantsPreparer;

    TransferDirection(Function<String, TransferParticipants> participantsPreparer) {
        this.participantsPreparer = participantsPreparer;
    }

    public TransferParticipants prepareParticipants(String adminToken) {
        return participantsPreparer.apply(adminToken);
    }
}
