package core.aop.invocation;

import java.lang.reflect.Method;

/**
 * Represents a method call "in progress".
 * Interceptors can call proceed() to continue the chain.
 */
public interface MethodInvocationInterface {
    Object proceed() throws Throwable;

    Object getThis();

    Method getMethod();

    Object[] getArguments();
}
