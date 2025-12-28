package core.bpp.aware;


import core.factory.BeanFactoryInterface;

/**
 * BeanFactoryAware
 *
 * Built-in Spring concept:
 * - If a bean implements this interface, the container injects the BeanFactory.
 *
 */
public interface BeanFactoryAware {

    void setBeanFactory(BeanFactoryInterface beanFactory);

}
