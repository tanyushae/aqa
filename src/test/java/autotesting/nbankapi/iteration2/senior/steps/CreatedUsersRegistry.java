package autotesting.nbankapi.iteration2.senior.steps;

import java.util.ArrayList;
import java.util.List;

public final class CreatedUsersRegistry {
    private CreatedUsersRegistry() {
    }

    private static final ThreadLocal<List<Integer>> USERS = ThreadLocal.withInitial(ArrayList::new);

    public static void registerUser(int userId) {
        USERS.get().add(userId);
    }

    public static List<Integer> getAndClearUserIds() {
        List<Integer> userIds = new ArrayList<>(USERS.get());
        USERS.remove();
        return userIds;
    }
}
