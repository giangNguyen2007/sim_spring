package core.factory.factory_post_processors;

import core.factory.SimpleBeanFactory;

/**
 * Called inside AppContext
 * Runs after BeanDefinitions are registered but before singleton instantiation.
 */
public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(SimpleBeanFactory beanFactory);
}
