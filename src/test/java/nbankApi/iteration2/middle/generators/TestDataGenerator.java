package nbankApi.iteration2.middle.generators;

import java.util.UUID;

public class TestDataGenerator {
    private TestDataGenerator() {
    }

    public static String generateUserName(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 4);

    }
}
