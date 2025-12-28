package core.aop.interceptor;

import core.aop.invocation.MethodInvocationInterface;

/**
 * Adapter: allows a MethodBeforeAdvice to participate in the MethodInterceptor chain.
 *
 * Chain order becomes:
 *   beforeAdvice.before(...)
 *   -> invocation.proceed()
 */
public class MethodBeforeAdviceInterceptor implements MethodInterceptorInterface {

    private final MethodBeforeAdviceInterface advice;

    public MethodBeforeAdviceInterceptor(MethodBeforeAdviceInterface advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocationInterface invocation) throws Throwable {

        advice.before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());

        return invocation.proceed();
    }
}
