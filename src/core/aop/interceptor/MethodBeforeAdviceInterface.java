package core.aop.interceptor;

import java.lang.reflect.Method;

/**
 * Second advice/interceptor type: runs BEFORE the target method.
 * It cannot decide whether the method continues; it just executes logic.
 *
 * (Spring has MethodBeforeAdvice with similar signature.)
 */
public interface MethodBeforeAdviceInterface {
    void before(Method method, Object[] args, Object target) throws Throwable;
}
