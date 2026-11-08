package autotesting.nbankapi.iteration2.senior.customers;

import java.util.function.Function;

import static autotesting.nbankapi.iteration2.senior.constants.TestData.NON_EXISTENT_ACCOUNT_ID;

public enum AccountSelector {

    OWN_ACCOUNT(parties -> parties.requester().getAccountId()),
    OTHER_USER_ACCOUNT(parties -> parties.anotherCustomer().getAccountId()),
    NON_EXISTENT_ACCOUNT(parties -> NON_EXISTENT_ACCOUNT_ID);

    private final Function<TestParties, Integer> accountIdResolver;

    AccountSelector(Function<TestParties, Integer> accountIdResolver) {
        this.accountIdResolver = accountIdResolver;
    }

    public int resolveAccountId(TestParties parties) {
        return accountIdResolver.apply(parties);
    }
}
