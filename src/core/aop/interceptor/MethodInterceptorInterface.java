package core.aop.interceptor;

import core.aop.invocation.MethodInvocationInterface;

/**
 * "Around" advice.
 * You can implement logging, metrics, transactions, etc.
 */
public interface MethodInterceptorInterface {
    Object invoke(MethodInvocationInterface invocation) throws Throwable;
}
