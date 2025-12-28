package core.bpp.interfaces;

/**
 * Extension of BeanPostProcessor for early exposure.
 * This is the key to make AOP + circular dependency work.
 *
 * Without this, your 3-level cache might expose raw beans early,
 * and AOP would be "lost" for the dependency that received the early ref.
 */
public interface SmartInstantiationAwareBeanPostProcessor extends BeanPostProcessorInterface {

    /**
     * Called when the factory needs an early reference for a singleton
     * (typically to resolve circular dependencies).
     */
    Object getEarlyBeanReference(Object bean, String beanName);
}
