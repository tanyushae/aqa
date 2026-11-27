package autotesting.nbankui.junior.enums;

import autotesting.nbankapi.iteration2.middle.support.steps.UserWithAccount;

public enum RecipientAccount {
    ANOTHER_USER {
        @Override
        public String accountNumber(UserWithAccount anotherUser) {
            return anotherUser.getAccount().getAccountNumber();
        }
    },
    NON_EXISTENT {
        @Override
        public String accountNumber(UserWithAccount anotherUser) {
            return NON_EXISTENT_ACCOUNT_NUMBER;
        }
    };

    private static final String NON_EXISTENT_ACCOUNT_NUMBER = "ACC999999";

    public abstract String accountNumber(UserWithAccount anotherUser);
}
