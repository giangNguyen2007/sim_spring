package core.aop.interceptor;

import core.aop.invocation.MethodInvocationInterface;

/**
 * Example interceptor: logs before/after.
 */
// Around advice : logging before and after of invocation.proceed()
public class LoggingInterceptor implements MethodInterceptorInterface {
    @Override
    public Object invoke(MethodInvocationInterface invocation) throws Throwable {
        long t0 = System.nanoTime();
        String sig = invocation.getThis().getClass().getSimpleName() + "." + invocation.getMethod().getName();

        System.out.println("[LOG] -> " + sig);

        try {

            // call the next interceptor in the chain, or the target method if at the end of the chain
            Object res = invocation.proceed();
            long dt = System.nanoTime() - t0;
            System.out.println("[LOG] <- " + sig + " (" + dt / 1_000_000.0 + " ms)");
            return res;

        } catch (Throwable ex) {
            System.out.println("[LOG] !! " + sig + " threw " + ex);
            throw ex;
        }
    }
}
