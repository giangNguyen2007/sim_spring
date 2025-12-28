package core.aop.interceptor;

import java.lang.reflect.Method;

public class SecurityCheckBeforeAdvice implements MethodBeforeAdviceInterface {
    @Override
    public void before(Method method, Object[] args, Object target) {

        System.out.println("[SEC] checking access for " +
                target.getClass().getSimpleName() + "." + method.getName());
        // Later you can throw to block execution:
        // throw new SecurityException("Forbidden");
    }
}
