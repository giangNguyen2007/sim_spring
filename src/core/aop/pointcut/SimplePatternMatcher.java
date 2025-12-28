package core.aop.pointcut;

/**
 * Very small matcher with two wildcards:
 * - '*' matches any chars (greedy)
 * - '..' matches any package depth
 *
 * Start minimal; improve later.
 */
public final class SimplePatternMatcher {
    private SimplePatternMatcher() {}

    public static boolean match(String pattern, String text) {
        // A pragmatic approach: convert mini pattern to regex.
        // pattern examples are like: "com.demo..service..*".
        String regex = pattern
                .replace(".", "\\.")
                .replace("\\.\\.", "(.+\\.)*")
                .replace("*", ".*");
        return text.matches(regex);
    }
}
