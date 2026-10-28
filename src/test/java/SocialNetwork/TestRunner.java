package SocialNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

public class TestRunner {
    public static void main(String[] args) {
        Lab4Test testInstance = new Lab4Test();
        Method[] methods = Lab4Test.class.getDeclaredMethods();

        Method beforeEach = Arrays.stream(methods)
                .filter(m -> m.isAnnotationPresent(BeforeEach.class))
                .findFirst()
                .orElse(null);

        int passed = 0;
        int failed = 0;

        for (Method m : methods) {
            if (m.isAnnotationPresent(Test.class)) {
                try {
                    if (beforeEach != null) {
                        beforeEach.invoke(testInstance);
                    }
                    m.invoke(testInstance);
                    System.out.println("[PASS] " + m.getName());
                    passed++;
                } catch (Throwable t) {
                    System.err.println("[FAIL] " + m.getName() + ": " + (t.getCause() != null ? t.getCause().getMessage() : t.getMessage()));
                    failed++;
                }
            }
        }

        System.out.println("\nTests finished: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
