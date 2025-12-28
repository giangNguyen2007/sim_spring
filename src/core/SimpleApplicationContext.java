package core;

import core.aop.advisors.AdvisorInterface;
import core.aop.advisors.DefaultPointcutAdvisor;
import core.aop.interceptor.LoggingInterceptor;
import core.aop.interceptor.MethodInterceptorInterface;
import core.aop.interceptor.TransactionInterceptor;
import core.aop.pointcut.AnnotationTransactionPointcut;
import core.aop.pointcut.NameMatchMethodPointcut;
import core.aop.pointcut.PointCutInterface;
import core.aop.transaction.SimpleTransactionManager;
import core.aop.transaction.TransactionManagerInterface;
import core.bpp.AopAutoProxyCreatorPostProcessor;
import core.bpp.common_annotations.PostConstructBeanPostProcessor;
import core.bpp.common_annotations.PreDestroyBeanPostProcessor;
import core.bpp.aware.AwareBeanPostProcessor;
import core.interfaces.BeanFactoryPostProcessor;
import core.factory.SimpleBeanFactory;

import java.util.ArrayList;
import java.util.List;

public class SimpleApplicationContext {

    private final SimpleBeanFactory beanFactory = new SimpleBeanFactory();

    // Programmatically registered post processors (like Spring's addBeanFactoryPostProcessor)
    private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();
    private boolean refreshed = false;

    public void registerBean(String name, BeanDefinition def) {
        if (refreshed) throw new IllegalStateException("Context already refreshed");
        beanFactory.registerBeanDefinition(name, def);
    }

    public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor bfpp) {
        this.beanFactoryPostProcessors.add(bfpp);
    }

    private void invokeBeanFactoryPostProcessors() {
        for (BeanFactoryPostProcessor bfpp : beanFactoryPostProcessors) {
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
        for (BeanFactoryPostProcessor bfpp : beanFactoryPostProcessors) {
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
