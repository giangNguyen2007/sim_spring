package core.aop.advisors;


import core.aop.interceptor.MethodInterceptorInterface;
import core.aop.pointcut.PointCutInterface;

/**
 * AOP = Advisor (Pointcut + Interceptor).
 */

// Contracts:  an advisor must provide a pointcut and an interceptor.
public interface AdvisorInterface {
    PointCutInterface getPointcut();

    MethodInterceptorInterface getInterceptor();
}
