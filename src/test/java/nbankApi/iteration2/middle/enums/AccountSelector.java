package nbankApi.iteration2.middle.enums;

import java.util.function.IntBinaryOperator;

import static nbankApi.iteration2.middle.constants.ApiConstants.NON_EXISTENT_ACCOUNT_ID;

/**
 * Какой счёт подставить в запрос: свой, счёт другого пользователя или несуществующий
 */
public enum AccountSelector {
    OWN_ACCOUNT((ownAccountId, anotherUserAccountId) -> ownAccountId),
    ANOTHER_USER_ACCOUNT((ownAccountId, anotherUserAccountId) -> anotherUserAccountId),
    NON_EXISTENT_ACCOUNT((ownAccountId, anotherUserAccountId) -> NON_EXISTENT_ACCOUNT_ID);

    private final IntBinaryOperator accountIdSelector;

    AccountSelector(IntBinaryOperator accountIdSelector) {
        this.accountIdSelector = accountIdSelector;
    }

    public int selectAccountId(int ownAccountId, int anotherUserAccountId) {
        return accountIdSelector.applyAsInt(ownAccountId, anotherUserAccountId);
    }
}
