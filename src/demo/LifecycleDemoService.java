package demo;

import core.annotations.PostConstruct;
import core.annotations.PreDestroy;

/**
 * LifecycleDemoService
 *
 * This bean demonstrates:
 * - @PostConstruct (handled by PostConstructBeanPostProcessor)
 * - init method configured in BeanDefinition
 * - @PreDestroy (handled by PreDestroyBeanPostProcessor during ctx.close())
 * - destroy method configured in BeanDefinition
 */
public class LifecycleDemoService {

    private boolean ready = false;

    public LifecycleDemoService() {
        System.out.println("[LifecycleDemoService] constructor");
    }

    @PostConstruct
    public void postConstruct() {
        // to be called by PostConstructBeanPostProcessor
        System.out.println("[LifecycleDemoService] @PostConstruct");
        ready = true;
    }

    // configured via BeanDefinition.setInitMethod("init")
    public void init() {
        System.out.println("[LifecycleDemoService] init-method (BeanDefinition)");
    }

    public String hello() {
        return ready ? "hello (ready=true)" : "hello (ready=false)";
    }

    @PreDestroy
    public void preDestroy() {
        // to be called by PreDestroyBeanPostProcessor
        System.out.println("[LifecycleDemoService] @PreDestroy");
    }

    // configured via BeanDefinition.setDestroyMethod("destroy")
    public void destroy() {
        System.out.println("[LifecycleDemoService] destroy-method (BeanDefinition)");
    }
}