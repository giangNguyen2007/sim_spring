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

        // =============== add AOP BPP (logging) ======================

        AopAutoProxyCreatorPostProcessor apc = new AopAutoProxyCreatorPostProcessor();

        // build an advisor: match methods by name, apply logging
        NameMatchMethodPointcut pc = new NameMatchMethodPointcut("Service")
                .addMethodName("placeOrder")
                .addMethodName("pay");

        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();

        // any class with name containing "service"
        // in this class, method named "pay", "placeOrder" will be intercepted by the logging interceptor
        AdvisorInterface loggingAdvisor = new DefaultPointcutAdvisor(pc, loggingInterceptor);
        apc.addAdvisor(loggingAdvisor);


        // =============== add AOP BPP (Transaction) ======================


        // Create transaction infrastructure
        TransactionManagerInterface txManager = new SimpleTransactionManager();
        MethodInterceptorInterface txInterceptor = new TransactionInterceptor(txManager);

        // Create advisor: @Transactional pointcut + tx interceptor
        PointCutInterface txPointcut = new AnnotationTransactionPointcut();
        // create advisor
        AdvisorInterface txAdvisor = new DefaultPointcutAdvisor(txPointcut, txInterceptor);

        apc.addAdvisor(txAdvisor);

        // register as a BeanPostProcessor in your factory
        beanFactory.addBeanPostProcessor(apc);

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
