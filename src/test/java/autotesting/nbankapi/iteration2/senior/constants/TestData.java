package autotesting.nbankapi.iteration2.senior.constants;

public class TestData {
    private TestData() {
    }

    public static final double MAX_DEPOSIT_AMOUNT = 5000.00;
    public static final double ACCOUNT_FUNDING_AMOUNT = MAX_DEPOSIT_AMOUNT;
    public static final double ZERO_AMOUNT = 0.00;
    public static final double NEGATIVE_AMOUNT = -0.01;
    public static final int NON_EXISTENT_ACCOUNT_ID = Integer.MAX_VALUE;
    public static final double INITIAL_BALANCE_NEW_ACCOUNT = 0.00;
    public static final double MONEY_TOLERANCE = 0.001;
}
