package core.factory.factory_post_processors;

import core.aop.advisors.AdvisorInterface;
import core.aop.advisors.DefaultPointcutAdvisor;
import core.aop.annotations.Around;
import core.aop.annotations.Aspect;
import core.aop.interceptor.AspectJMethodInterceptor;
import core.aop.interceptor.MethodInterceptorInterface;
import core.aop.pointcut.ExecutionPointcut;
import core.aop.pointcut.PointCutInterface;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Converts an @Aspect bean instance into a list of Advisors.
 */
public class AspectAdvisorFactory {

    public List<AdvisorInterface> buildAdvisors(Object aspectBean) {
        Class<?> aspectClass = aspectBean.getClass();

        if (!aspectClass.isAnnotationPresent(Aspect.class)) {
            return List.of();
        }

        List<AdvisorInterface> advisors = new ArrayList<>();

        // iterate over all methods of the bean annotated with @Aspect
        for (Method m : aspectClass.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Around.class)) {
                continue;
            }

            // if method is annotated with @Around, create advisor from the method
            Around around = m.getAnnotation(Around.class);

            // retrieve the expression associated with @Around annotation "execution:..."
            String expr = around.value();

            // Minimal: expect prefix "execution:"
            if (!expr.startsWith("execution:")) {
                throw new IllegalArgumentException("Unsupported pointcut expression: " + expr);
            }

            // extract the pattern after "execution:"
            String pattern = expr.substring("execution:".length());

            // crate a pointcut from the pattern
            PointCutInterface pc = new ExecutionPointcut(pattern);

            MethodInterceptorInterface interceptor = new AspectJMethodInterceptor(aspectBean, m);

            // create advisors with
            // interceptor = the method annotated with @Around
            // pointcut = checked based on the pattern
            advisors.add(new DefaultPointcutAdvisor(pc, interceptor));
        }

        return advisors;
    }
}
