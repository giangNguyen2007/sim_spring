package core;

public class SimpleApplicationContext {

    private final SimpleBeanFactory beanFactory = new SimpleBeanFactory();
    private boolean refreshed = false;

    public void registerBean(String name, BeanDefinition def) {
        if (refreshed) throw new IllegalStateException("Context already refreshed");
        beanFactory.registerBeanDefinition(name, def);
    }

    // initialize all singletons beans
    public void refresh() {
        if (refreshed) return;
        beanFactory.preInstantiateSingletons();
        refreshed = true;
    }

    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    public <T> T getBean(Class<T> type) {
        return beanFactory.getBean(type);
    }

    public void close() {
        beanFactory.close();
    }
}
