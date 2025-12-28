package core.bpp.aware;

import core.interfaces.BeanFactoryInterface;
import core.bpp.interfaces.BeanPostProcessorInterface;

/**
 * AwareBeanPostProcessor (built-in)
 *
 * gng : This is a container-internal BeanPostProcessor.
 * It injects container metadata into beans implementing "Aware" interfaces.
 *
 * - BeanNameAware     -> setBeanName(beanName)
 * - BeanFactoryAware  -> setBeanFactory(beanFactory)
 *
 * We do it in postProcessBeforeInitialization():
 * - after dependencies are injected (populateBean)
 * - before init-method runs
 */
public class AwareBeanPostProcessor implements BeanPostProcessorInterface {

    private final BeanFactoryInterface beanFactory;

    public AwareBeanPostProcessor(BeanFactoryInterface beanFactory) {
        if (beanFactory == null) throw new IllegalArgumentException("beanFactory must not be null");
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        // gng : inject bean name if bean wants it
        if (bean instanceof BeanNameAware) {

            System.out.println("AwareBeanPostProcessor: injecting bean name '" + beanName + "' into " + bean.getClass().getSimpleName());

            ((BeanNameAware) bean).setBeanName(beanName);
        }

        // gng : inject BeanFactory if bean wants it
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(beanFactory);
        }

        return bean;
    }
}
