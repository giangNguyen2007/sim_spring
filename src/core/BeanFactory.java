package core;

public interface BeanFactory {
    void registerBeanDefinition(String name, BeanDefinition def);

    Object getBean(String name);

    <T> T getBean(Class<T> type);
}
