package core.factory.factory_post_processors;

import java.lang.reflect.Modifier;
import java.util.*;


import core.BeanDefinition;
import core.annotations.Component;
import core.context.BeanNameGenerator;
import core.context.ClassPathScanner;
import core.factory.SimpleBeanFactory;

/**
 * BFPP: register BeanDefinitions discovered by scanning.
 */
public class ComponentScanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private final List<String> basePackages;
    private final ClassPathScanner scanner;

    public  ComponentScanFactoryPostProcessor(List<String> basePackages, ClassLoader cl) {
        this.basePackages = basePackages;
        this.scanner = new ClassPathScanner(cl);
    }

    @Override
    public void postProcessBeanFactory(SimpleBeanFactory beanFactory) {

        // iterate over base packages
        for (String pkg : basePackages) {

            // get all classes in package using scanner
            Set<Class<?>> classes = scanner.scan(pkg);

            // iterate over all classes, register those annotated with @Component to the bean factory
            for (Class<?> c : classes) {
                Component comp = c.getAnnotation(Component.class);
                if (comp == null) continue;

                // Skip types that cannot be instantiated
                if (c.isInterface()) continue;
                if (Modifier.isAbstract(c.getModifiers())) continue;

                String name = comp.value();
                if (name == null || name.isBlank()) {
                    name = BeanNameGenerator.defaultName(c);
                }

                // Don’t override explicit registrations
                if (beanFactory.containsBeanDefinition(name)) {
                    continue;
                }

                // Minimal: BeanDefinition holds beanClass

                System.out.println("ComponentScanFactoryPostProcessor: registering component: " + name + " -> " + c.getName());
                BeanDefinition bd = new BeanDefinition(c);
                beanFactory.registerBeanDefinition(name, bd);
            }
        }
    }
}
