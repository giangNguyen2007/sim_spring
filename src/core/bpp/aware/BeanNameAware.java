package core.bpp.aware;

/**
 * BeanNameAware
 *
 * Built-in Spring concept:
 * - If a bean implements this interface, the container injects its bean name.
 * - This is useful for beans that need to know their own name in the container.
 */
public interface BeanNameAware {

    void setBeanName(String beanName);

}
