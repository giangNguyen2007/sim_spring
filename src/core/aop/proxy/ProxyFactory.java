package core.aop.proxy;

import core.aop.advisors.AdvisorInterface;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Creates a JDK dynamic proxy.
 * Limitation: target must implement at least one interface.
 * (Spring also supports CGLIB subclass proxies; you can add later.)
 */

// input = target object + list of advisors
// returns => proxy object
public class ProxyFactory {

    public static Object createJdkProxy(Object targetBean, List<AdvisorInterface> advisors) {
        Class<?> targetClass = targetBean.getClass();
        Class<?>[] ifaces = targetClass.getInterfaces();

        if (ifaces.length == 0) {
            // Keep it simple for now.
            // In Spring, we'd fallback to CGLIB.
            return targetBean;
        }

        return Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                ifaces,
                new JdkDynamicAopProxy(targetBean, advisors)
        );
    }
}