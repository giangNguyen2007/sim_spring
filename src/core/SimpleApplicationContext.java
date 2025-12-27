package core;

import core.bpp.common_annotations.PostConstructBeanPostProcessor;
import core.bpp.common_annotations.PreDestroyBeanPostProcessor;
import core.bpp.aware.AwareBeanPostProcessor;

public class SimpleApplicationContext {

    private final SimpleBeanFactory beanFactory = new SimpleBeanFactory();
    private boolean refreshed = false;

    public void registerBean(String name, BeanDefinition def) {
        if (refreshed) throw new IllegalStateException("Context already refreshed");
        beanFactory.registerBeanDefinition(name, def);
    }

    // main engine method
    public void refresh() {
        if (refreshed) return;

        // ============================================================
        // Phase 0) Register built-in BeanPostProcessors (infrastructure)
        // ------------------------------------------------------------
        // gng : Spring registers some internal BPPs by default.
        // We start with the "Aware" processor.
        // ============================================================
        beanFactory.addBeanPostProcessor(new AwareBeanPostProcessor(beanFactory));

        beanFactory.addBeanPostProcessor(new PostConstructBeanPostProcessor()); // gng : @PostConstruct
        beanFactory.addBeanPostProcessor(new PreDestroyBeanPostProcessor());

        // ============================================================
        // Phase 1) Register user-defined BeanPostProcessor beans
        // ------------------------------------------------------------
        // gng : must happen BEFORE preInstantiateSingletons()
        // ============================================================
        beanFactory.registerBeanPostProcessors();


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
