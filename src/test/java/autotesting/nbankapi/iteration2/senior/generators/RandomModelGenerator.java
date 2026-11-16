package autotesting.nbankapi.iteration2.senior.generators;

import com.github.curiousoddman.rgxgen.RgxGen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RandomModelGenerator {

    public static <T> T generate(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : getAllFields(clazz)) {
                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
                if (rule == null) {
                    continue;
                }
                field.setAccessible(true);
                field.set(instance, new RgxGen(rule.regex()).generate());
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate entity " + clazz.getSimpleName(), e);
        }
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(List.of(c.getDeclaredFields()));
        }
        return fields;
    }
}
