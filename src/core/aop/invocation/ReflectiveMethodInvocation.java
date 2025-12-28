package core.aop.invocation;

import core.aop.interceptor.MethodInterceptorInterface;

import java.lang.reflect.Method;
import java.util.List;

/**
 * MethodInvocation implementation that supports an interceptor chain.
 *
 * This is intentionally framework-like (similar to Spring's ReflectiveMethodInvocation),
 * so you can reuse it for JDK proxy and later CGLIB proxy.
 */
public class ReflectiveMethodInvocation implements MethodInvocationInterface {

    private final Object target;
    private final Method method;
    private final Object[] args;
    private final List<MethodInterceptorInterface> interceptorChain;

    /**
     * Current interceptor index in the chain.
     * Starts at -1 so the first proceed() moves it to 0.
     */
    private int index = -1;

    public ReflectiveMethodInvocation(Object target,
                                      Method method,  // method to be intercepted
                                      Object[] args,  // arguments for calling method at the end of interceptor chain
                                      List<MethodInterceptorInterface> chain) {
        this.target = target;
        this.method = method;
        this.args = (args != null ? args : new Object[0]);
        this.interceptorChain = chain;
    }

    @Override
    public Object proceed() throws Throwable {
        index++;
        // End of chain -> invoke the real target method
        if (index == interceptorChain.size()) {
            return method.invoke(target, args);
        }

        // extract current interceptor
        MethodInterceptorInterface currentInterceptor = interceptorChain.get(index);

        // make the incerceptor intercept  invocation.proceed() => intercept next interceptor or the target's method
        // the target's method is always called last
        // each interceptor passed up the target method's result to upper interceptor
        Object res = currentInterceptor.invoke(this);

        return res;
    }

    @Override
    public Object getThis() {
        return target;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return args;
    }
}
