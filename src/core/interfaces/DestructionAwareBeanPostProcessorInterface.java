package core.interfaces;

/**
 * DestructionAwareBeanPostProcessor
 *
 * gng : BPP extension for destruction phase.
 * This is how we implement @PreDestroy in a clean way:
 * - init hooks stay in BeanPostProcessor
 * - destroy hooks go here
 */
public interface DestructionAwareBeanPostProcessorInterface extends BeanPostProcessorInterface {

    /**
     * Return true if this processor needs destruction callback for the given bean.
     * Default: true (safe, simple).
     */
    default boolean requiresDestruction(Object bean) {
        return true;
    }

    /**
     * Called before bean destruction (context close).
     */
    default void postProcessBeforeDestruction(Object bean, String beanName) {
        // no-op by default
    }
}
