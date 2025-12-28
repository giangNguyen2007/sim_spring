package core.aop.joinpoint;

import core.aop.invocation.MethodInvocationInterface;

import java.lang.reflect.Method;

public class DefaultProceedingJoinPoint implements ProceedingJoinPoint {

    private final MethodInvocationInterface invocation;

    public DefaultProceedingJoinPoint(MethodInvocationInterface invocation) {
        this.invocation = invocation;
    }

    @Override
    public Object proceed() throws Throwable {
        return invocation.proceed();
    }

    @Override
    public Object getTarget() {
        return invocation.getThis();
    }

    @Override
    public Method getMethod() {
        return invocation.getMethod();
    }

    @Override
    public Object[] getArgs() {
        return invocation.getArguments();
    }
}
