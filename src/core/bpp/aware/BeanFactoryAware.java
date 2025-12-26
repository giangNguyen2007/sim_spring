package core.bpp.aware;


import core.interfaces.BeanFactory;

/**
 * BeanFactoryAware
 *
 * Built-in Spring concept:
 * - If a bean implements this interface, the container injects the BeanFactory.
 *
 */
public interface BeanFactoryAware {

    void setBeanFactory(BeanFactory beanFactory);

}
