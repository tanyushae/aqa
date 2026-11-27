package autotesting.nbankui.junior;

public final class UiConstants {
    private UiConstants() {
    }

    public static final String LOGIN_PAGE_PATH = "/";

    public static final String LOGIN_BUTTON_TEXT = "Login";
    public static final String USER_DASHBOARD_TEXT = "User Dashboard";
    public static final String NONAME_TEXT = "Noname";
    public static final String DEPOSIT_MONEY_TEXT = "Deposit Money";
    public static final String EDIT_PROFILE_TEXT = "Edit Profile";
    public static final String MAKE_A_TRANSFER_TEXT = "Make a Transfer";

    // Шаблоны: %s заменяется значениями при вызове .formatted(...)
    public static final String ACCOUNT_OPTION_TEMPLATE = "%s (Balance: $%s)";
    public static final String USERNAME_HEADER_TEMPLATE = "@%s";
    public static final String TRANSFER_SUCCESS_TEMPLATE = "✅ Successfully transferred $%s to account %s!";

    public static final String ZERO_BALANCE_UI = "0.00";
}
