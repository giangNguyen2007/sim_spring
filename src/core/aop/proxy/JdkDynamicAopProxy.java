package core.aop.proxy;

import core.aop.advisors.AdvisorInterface;
import core.aop.interceptor.MethodInterceptorInterface;
import core.aop.invocation.ReflectiveMethodInvocation;
import core.aop.pointcut.PointCutInterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * JDK InvocationHandler that runs interceptors when pointcuts match.
 */

// one Proxy = wrapper over one bean
public class JdkDynamicAopProxy implements InvocationHandler {

    private final Object targetBean;

    private Class<?> targetBeanClass;
    private final List<AdvisorInterface> advisors;

    public JdkDynamicAopProxy(Object targetBean, List<AdvisorInterface> advisors) {
        this.targetBean = targetBean;
        this.targetBeanClass = targetBean.getClass();
        this.advisors = advisors;
    }

    @Override
    // invoke only on methods of the target
    // args => arguments for the target's method to be called
    //
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Don't advise Object methods (toString/equals/hashCode) by default.
        if (method.getDeclaringClass() == Object.class) {

            // invoke the method on the target, without calling interceptors
            return method.invoke(this.targetBean, args);
        }


        List<MethodInterceptorInterface> interceptorChain = new ArrayList<>();
        // Build the interceptor chain for this method.

        // iterate over all advisors
        for (AdvisorInterface advisor : this.advisors) {

            // Check if the pointcut of the advisor matches the target class and method.
            PointCutInterface pc = advisor.getPointcut();
            if (pc.matchesClass(targetBeanClass) && pc.matchesMethod(method, this.targetBeanClass)) {

                // if matched, add the interceptor to the chain.
                interceptorChain.add(advisor.getInterceptor());
            }
        }

        // No interceptors -> directly invoke the target method
        if (interceptorChain.isEmpty()) {
            return method.invoke(targetBean, args);
        }

        // if not empty
        // Build a simple interceptor chain.
        return new ReflectiveMethodInvocation(targetBean, method, args, interceptorChain).proceed();
    }

}
