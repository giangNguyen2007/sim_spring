package core.factory;

import core.BeanDefinition;

public interface BeanFactoryInterface {
    void registerBeanDefinition(String name, BeanDefinition def);

    Object getBean(String name);

    <T> T getBean(Class<T> type);
}
