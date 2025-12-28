package core.bpp;

import core.aop.advisors.AdvisorInterface;
import core.aop.interceptor.MethodInterceptorInterface;
import core.aop.interceptor.TransactionInterceptor;
import core.aop.pointcut.PointCutInterface;
import core.aop.proxy.ProxyFactory;
import core.aop.transaction.TransactionManagerInterface;
import core.bpp.interfaces.BeanPostProcessorInterface;
import core.bpp.interfaces.SmartInstantiationAwareBeanPostProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring-like AutoProxyCreator:
 * - wraps beans with proxies when they match Advisors
 * - IMPORTANT: also supports early proxy exposure for circular dependencies
 *
 * You can register advisors programmatically into this creator for now.
 * Later, you can auto-discover Advisor beans from the factory (ListableBeanFactory style).
 */
public class AopAutoProxyCreatorPostProcessor implements SmartInstantiationAwareBeanPostProcessor {

    /**
     * Advisors known to the container (pointcut + interceptor).
     * Keep it simple: user registers them.
     */
    private final List<AdvisorInterface> advisors = new ArrayList<>();

    /**
     * If a bean needs an early reference during singleton circular resolution,
     * we create the proxy once and reuse it.
     */
    private final Map<String, Object> earlyProxyReferences = new ConcurrentHashMap<>();

    // add advisors
    public void addAdvisor(AdvisorInterface advisor) {
        this.advisors.add(advisor);
    }

    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) {
        // If we already created an early proxy, reuse it.
        Object existing = earlyProxyReferences.get(beanName);
        if (existing != null) return existing;

        if (!shouldProxy(beanName, bean)) return bean;

        Object proxy = createBeanProxy(bean, beanName);
        // If createBeanProxy returned the same bean (no interfaces), we still store it for consistency.
        earlyProxyReferences.put(beanName, proxy);
        return proxy;
    }



    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // If this bean already got an early proxy (because of a circular dependency),
        // we must return the SAME proxy, not create a second one.
        Object early = earlyProxyReferences.get(beanName);
        if (early != null) {
            return early;
        }

        System.out.println("[ProxyBPP ] : Should Proxy for " + beanName + " =" + shouldProxy(beanName, bean));

        if (!shouldProxy(beanName, bean)) return bean;

        return createBeanProxy(bean, beanName);
    }


    // ============= Helper Method ============================

    private boolean shouldProxy(String beanName, Object bean) {
        // Avoid proxying infrastructure pieces (very important in real containers).
        if (bean instanceof AdvisorInterface) return false;
        if (bean instanceof MethodInterceptorInterface) return false;
        if (bean instanceof BeanPostProcessorInterface) return false;
        if (bean instanceof TransactionManagerInterface) return false;

        //
        Class<?> targetClass = bean.getClass();
        for (AdvisorInterface advisor : advisors) {

            PointCutInterface pc = advisor.getPointcut();


            if (pc.matchesClass(targetClass)) {
                // We proxy at class-level if any advisor could apply to any method.
                // (Later you can do more precise checks.)

                System.out.println("[ProxyBPP ] : Class matched for bean " + beanName + ", which will be processed for proxying by advisor " + advisor.getClass().getSimpleName());

                return true;
            }
        }
        return false;
    }

    private Object createBeanProxy(Object bean, String beanName) {
        // Create JDK proxy if possible; otherwise, fallback to raw bean.
        // (Later: add CGLIB subclass proxy support.)
        return ProxyFactory.createJdkProxy(bean, advisors);
    }
}
