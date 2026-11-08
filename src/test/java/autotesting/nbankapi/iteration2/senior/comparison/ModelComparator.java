package autotesting.nbankapi.iteration2.senior.comparison;

import autotesting.nbankapi.iteration2.senior.constants.TestData;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelComparator {
    private ModelComparator() {
    }

    public static <A, B> ComparisonResult compareFields(A request, B response, Map<String, String> fieldMappings) {
        List<Mismatch> mismatches = new ArrayList<>();

        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
            String requestField = entry.getKey();
            String responseField = entry.getValue();

            Object requestValue = getFieldValue(request, requestField);
            Object responseValue = getFieldValue(response, responseField);

            if (!areValuesEqual(requestValue, responseValue)) {
                mismatches.add(new Mismatch(requestField + " -> " + responseField, requestValue, responseValue));
            }
        }
        return new ComparisonResult(mismatches);
    }

    private static boolean areValuesEqual(Object requestValue, Object responseValue) {
        if (Objects.equals(requestValue, responseValue)) {
            return true;
        }
        if (requestValue == null || responseValue == null) {
            return false;
        }
        if (requestValue instanceof Number requestNumber && responseValue instanceof Number responseNumber) {
            return Math.abs(requestNumber.doubleValue() - responseNumber.doubleValue()) <= TestData.MONEY_TOLERANCE;
        }
        return Objects.equals(String.valueOf(requestValue), String.valueOf(responseValue));
    }

    private static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        if (fieldName.contains(".")) {
            String[] parts = fieldName.split("\\.", 2);
            Object nestedObject = getSingleFieldValue(obj, parts[0]);
            return getFieldValue(nestedObject, parts[1]);
        }
        return getSingleFieldValue(obj, fieldName);
    }

    private static Object getSingleFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) { // ищем в родительском классе
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + fieldName, e);
            }
        }
        throw new RuntimeException("Field '" + fieldName + "' not found in class " + obj.getClass().getName());
    }

    @Getter
    public static class ComparisonResult {
        private final List<Mismatch> mismatches;

        public ComparisonResult(List<Mismatch> mismatches) {
            this.mismatches = mismatches;
        }

        public boolean isSuccess() {
            return mismatches.isEmpty();
        }

        @Override
        public String toString() {
            if (isSuccess()) {
                return "All fields match.";
            }
            StringBuilder sb = new StringBuilder("Mismatched fields:\n");
            for (Mismatch m : mismatches) {
                sb.append("- ").append(m.fieldName())
                        .append(": expected=").append(m.expected())
                        .append(", actual=").append(m.actual()).append("\n");
            }
            return sb.toString();
        }
    }

    public record Mismatch(String fieldName, Object expected, Object actual) {
    }
}
