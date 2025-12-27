package core.interfaces;

/**
 * BeanPostProcessor (BPP)
 *
 * - This is the main extension hook around bean initialization.
 * - Typical usage: AOP proxies, injecting extra behavior, validation, etc.
 *
 * In real Spring, there are many sub-interfaces (Aware, SmartInstantiationAware, etc.).
 * For our mini-container, we keep ONE interface, with default methods.
 */
public interface BeanPostProcessorInterface {

    /**
     * Called after dependency injection (populateBean), but BEFORE init-method.
     *
     * You may return:
     * - the same bean
     * - or a wrapped bean (rare for "before" phase)
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * Called AFTER init-method.
     *
     * This is the most common place to return an AOP proxy.
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * Called when the bean is exposed early (3-level cache).
     *
     * Why we need this:
     * - In circular dependency situations, other beans may get an "early reference".
     * - If we plan to create proxies, we may want to expose the proxy EARLY so
     *   that injected references are consistent.
     *
     * If you don't do AOP, just return the bean.
     */
    default Object getEarlyBeanReference(Object bean, String beanName) {
        return bean;
    }
}