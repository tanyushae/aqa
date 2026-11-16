package autotesting.nbankapi.iteration2.senior.comparison;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ModelComparisonConfigLoader {

    private static ModelComparisonConfigLoader instance;
    private final Map<String, ComparisonRule> rules = new HashMap<>();

    private ModelComparisonConfigLoader() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("model-comparison.properties")) {
            if (input == null) {
                throw new IllegalArgumentException("model-comparison.properties not found in resources");
            }
            Properties props = new Properties();
            props.load(input);

            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                String[] parts = value.split(":");
                if (parts.length != 2) {
                    throw new IllegalArgumentException(
                            "Wrong rule format in model-comparison.properties: " + key + "=" + value);
                }
                rules.put(key.trim(), new ComparisonRule(Arrays.asList(parts[1].split(","))));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DTO comparison config from model-comparison.properties", e);
        }
    }

    public static synchronized ModelComparisonConfigLoader getInstance() {
        if (instance == null) {
            instance = new ModelComparisonConfigLoader();
        }
        return instance;
    }

    public ComparisonRule getRuleFor(Class<?> requestClass) {
        return rules.get(requestClass.getSimpleName());
    }

    @Getter
    public static class ComparisonRule {
        private final Map<String, String> fieldMappings;

        public ComparisonRule(List<String> fieldPairs) {
            this.fieldMappings = new HashMap<>();

            for (String pair : fieldPairs) {
                String[] parts = pair.split("=");
                if (parts.length != 2) {
                    throw new IllegalArgumentException(
                            "Wrong field mapping '" + pair + "', expected requestField=responseField");
                }
                fieldMappings.put(parts[0].trim(), parts[1].trim());
            }
        }
    }
}
