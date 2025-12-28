package core.context;

public final class BeanNameGenerator {
    private BeanNameGenerator() {}

    public static String defaultName(Class<?> type) {
        String s = type.getSimpleName();
        if (s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
