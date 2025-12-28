package core.aop.advisors;

import core.aop.interceptor.MethodInterceptorInterface;
import core.aop.pointcut.PointCutInterface;

public class DefaultPointcutAdvisor implements AdvisorInterface {
    private final PointCutInterface pointcut;
    private final MethodInterceptorInterface interceptor;

    public DefaultPointcutAdvisor(PointCutInterface pointcut, MethodInterceptorInterface interceptor) {
        this.pointcut = pointcut;
        this.interceptor = interceptor;
    }

    @Override
    public PointCutInterface getPointcut() {
        return pointcut;
    }

    @Override
    public MethodInterceptorInterface getInterceptor() {
        return interceptor;
    }
}
