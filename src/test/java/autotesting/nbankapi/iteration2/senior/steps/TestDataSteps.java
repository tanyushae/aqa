package autotesting.nbankapi.iteration2.senior.steps;

import autotesting.nbankapi.iteration2.senior.customers.CustomerWithAccount;
import autotesting.nbankapi.iteration2.senior.customers.CustomerWithTwoAccounts;
import autotesting.nbankapi.iteration2.senior.models.account.CreateAccountResponseDto;
import io.restassured.specification.RequestSpecification;

public class TestDataSteps {
    private TestDataSteps() {
    }

    public static CustomerWithAccount createNewUserWithAccount() {
        RequestSpecification authSpec = UserSteps.createUserAndAuthenticate();
        CreateAccountResponseDto account = AccountSteps.createAccount(authSpec);
        return new CustomerWithAccount(authSpec, account);
    }

    public static CustomerWithAccount createNewUserWithFundedAccount(double depositAmount) {
        return createNewUserWithFundedAccount(depositAmount, 1);
    }

    public static CustomerWithAccount createNewUserWithFundedAccount(double depositAmount, int depositsCount) {
        CustomerWithAccount customer = createNewUserWithAccount();
        fundAccount(customer.getAuthSpec(), customer.getAccountId(), depositAmount, depositsCount);
        return customer;
    }

    public static CustomerWithTwoAccounts createNewUserWithTwoAccountsAndFundedFirst(double firstAccountDeposit) {
        CustomerWithAccount firstAccount = createNewUserWithFundedAccount(firstAccountDeposit);
        CreateAccountResponseDto secondAccount = AccountSteps.createAccount(firstAccount.getAuthSpec());
        return new CustomerWithTwoAccounts(firstAccount.getAuthSpec(), firstAccount.getAccount(), secondAccount);
    }

    private static void fundAccount(RequestSpecification authSpec, int accountId, double amount, int depositsCount) {
        for (int i = 0; i < depositsCount; i++) {
            DepositSteps.deposit(authSpec, accountId, amount);
        }
    }
}
