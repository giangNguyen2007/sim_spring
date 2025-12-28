package core.aop.pointcut;

import java.lang.reflect.Method;

/**
 * Decides whether an advisor applies to a given class/method.
 * We keep it simple: classMatch + methodMatch.
 */
public interface PointCutInterface {
    boolean matchesClass(Class<?> targetClass);

    boolean matchesMethod(Method method, Class<?> targetClass);
}
