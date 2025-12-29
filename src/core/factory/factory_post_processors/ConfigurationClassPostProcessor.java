package core.factory.factory_post_processors;
import java.lang.reflect.Method;
import java.util.*;

import core.BeanDefinition;
import core.annotations.Bean;
import core.annotations.Configuration;
import core.factory.SimpleBeanFactory;

public class ConfigurationClassPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(SimpleBeanFactory beanFactory) {

        // find all names of all beans annotated with @Configuration
        List<String> configNames = beanFactory.findBeanNamesByAnnotation(Configuration.class);

        // iterate over each bean annotated with @Configuration
        for (String configBeanName : configNames) {

            BeanDefinition configBd = beanFactory.getBeanDefinition(configBeanName);

            // get the bean class
            Class<?> configClass = configBd.getBeanClass();

            // 2) Parse @Bean methods

            // iterate over all declared methods of the class
            for (Method m : configClass.getDeclaredMethods()) {
                // skip if not annotated with @Bean
                if (!m.isAnnotationPresent(Bean.class)) continue;

                // extract the @Bean annotation
                Bean beanAnn = m.getAnnotation(Bean.class);

                // retrieve the value (name) of the bean
                String beanName = beanAnn.value();
                if (beanName == null || beanName.isBlank()) {
                    beanName = m.getName(); // Spring default
                }

                // Avoid overriding explicit definitions
                if (beanFactory.containsBeanDefinition(beanName)) {
                    continue;
                }

                m.setAccessible(true);

                // get the return type of the method annnotated with @Bean
                Class<?> returnType = m.getReturnType();

                BeanDefinition bd = new BeanDefinition(returnType);

                // Supplier invokes @Bean method on config instance
                final String finalBeanName = beanName;
                final Method factoryMethod = m;
                final String cfgName = configBeanName;

                // register supplier that calls the @Bean method
                // which supplies the bean instance
                bd.setSupplier(() -> {
                    try {
                        Object cfg = beanFactory.getBean(cfgName);

                        // Resolve method args from container by type
                        Object[] args = resolveMethodArguments(beanFactory, factoryMethod);

                        // invoke the @Bean method on the config instance => returns the bean instance
                        return factoryMethod.invoke(cfg, args);
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to create @Bean '" + finalBeanName + "'", ex);
                    }
                });

                beanFactory.registerBeanDefinition(beanName, bd);
            }
        }
    }

    // Helper method to resolve method arguments from the container by type
    // for each parameter of the method
    // retrieve the bean of that type from the container
    private Object[] resolveMethodArguments(SimpleBeanFactory bf, Method m) {

        Class<?>[] paramTypes = m.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = bf.getBean(paramTypes[i]);
        }
        return args;
    }
}
