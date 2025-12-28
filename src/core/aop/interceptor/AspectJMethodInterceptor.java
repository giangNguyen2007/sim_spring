package core.aop.interceptor;

import core.aop.invocation.MethodInvocationInterface;
import core.aop.joinpoint.DefaultProceedingJoinPoint;
import core.aop.joinpoint.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * Adapts an @Around advice method into your MethodInterceptor chain.
 */
public class AspectJMethodInterceptor implements MethodInterceptorInterface {

    private final Object aspectInstance;
    private final Method adviceMethod;

    public AspectJMethodInterceptor(Object aspectInstance, Method adviceMethod) {
        // instance of the class annotated with @Aspect
        // not instance of the target bean to be proxied
        this.aspectInstance = aspectInstance;

        // aspect instance's method annotated with @Around
        this.adviceMethod = adviceMethod;
        this.adviceMethod.setAccessible(true);
    }

    @Override
    public Object invoke(MethodInvocationInterface invocation) throws Throwable {
        // Minimal rule: @Around advice method must accept exactly one param: ProceedingJoinPoint
        // Later: allow JoinPoint + arg binding.
        ProceedingJoinPoint pjp = new DefaultProceedingJoinPoint(invocation);

        return adviceMethod.invoke(aspectInstance, pjp);
    }
}
