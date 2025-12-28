package core.context;

import core.BeanDefinition;
import core.factory.factory_post_processors.AspectJAutoProxyRegistrarBFPP;
import core.factory.factory_post_processors.BeanFactoryPostProcessor;
import core.factory.SimpleBeanFactory;
import core.factory.factory_post_processors.ComponentScanFactoryPostProcessor;

import java.util.ArrayList;
import java.util.List;

public class SimpleApplicationContext {

    private final SimpleBeanFactory beanFactory = new SimpleBeanFactory();

    // Programmatically registered post processors (like Spring's addBeanFactoryPostProcessor)
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessorsList = new ArrayList<>();
    private boolean refreshed = false;

    public SimpleApplicationContext(String... basePackages) {
        // Register scanning BFPP FIRST
        beanFactoryPostProcessorsList.add(new ComponentScanFactoryPostProcessor(List.of(basePackages),
                Thread.currentThread().getContextClassLoader()));

        // Later, you add other BFPPs (AOP aspect registrar, @Configuration parser, ...)
        // bfpps.add(new AspectJAutoProxyRegistrar());

        //beanFactoryPostProcessorsList.add(new AspectJAutoProxyRegistrarBFPP());
    }



    public void registerBean(String name, BeanDefinition def) {
        if (refreshed) throw new IllegalStateException("Context already refreshed");
        beanFactory.registerBeanDefinition(name, def);
    }


    private void invokeBeanFactoryPostProcessors() {
        for (BeanFactoryPostProcessor bfpp : beanFactoryPostProcessorsList) {
            bfpp.postProcessBeanFactory(beanFactory);
        }

        // B) (optional) BFPPs declared as beans
        // If you support it, you can discover and instantiate them here.
        // Example:
        // for (String name : beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class)) {
        //     BeanFactoryPostProcessor bfppBean = beanFactory.getBean(name, BeanFactoryPostProcessor.class);
        //     bfppBean.postProcessBeanFactory(beanFactory);
        // }
    }

    // main engine method
    public void refresh() {
        if (refreshed) return;


        // ======================= pass BeanFactory by FactoryPostProcessors ===============
        // for clarity, write code instead of calling invokeBeanFactoryPostProcessors()
        for (BeanFactoryPostProcessor bfpp : beanFactoryPostProcessorsList) {
            bfpp.postProcessBeanFactory(beanFactory);
        }

        // ============================================================
        // Phase 0) Register built-in BeanPostProcessors (infrastructure)
        // ------------------------------------------------------------
        // gng : Spring registers some internal BPPs by default.
        // We start with the "Aware" processor.
        // ============================================================
//        beanFactory.addBeanPostProcessor(new AwareBeanPostProcessor(beanFactory));
//
//        beanFactory.addBeanPostProcessor(new PostConstructBeanPostProcessor()); // gng : @PostConstruct
//        beanFactory.addBeanPostProcessor(new PreDestroyBeanPostProcessor()); // gng : @PreDestroy

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
