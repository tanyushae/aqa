package autotesting.nbankapi.iteration2.senior.constants;

public class ApiMessages {
    private ApiMessages() {
    }

    public static final String TRANSFER_SUCCESS_MESSAGE = "Transfer successful";
    public static final String UPDATE_PROFILE_NAME_SUCCESS_MESSAGE = "Profile updated successfully";
    public static final String DEPOSIT_ABOVE_MAX_MESSAGE = "Deposit amount cannot exceed 5000";
    public static final String DEPOSIT_BELOW_MIN_MESSAGE = "Deposit amount must be at least 0.01";
    public static final String TRANSFER_ABOVE_MAX_MESSAGE = "Transfer amount cannot exceed 10000";
    public static final String TRANSFER_BELOW_MIN_MESSAGE = "Transfer amount must be at least 0.01";
    public static final String TRANSFER_INVALID_MESSAGE = "Invalid transfer: insufficient funds or invalid accounts";
    public static final String ACCOUNT_ACCESS_DENIED_MESSAGE = "Unauthorized access to account";
    public static final String INVALID_PROFILE_NAME_MESSAGE = "Name must contain two words with letters only";
}
