package core.factory.factory_post_processors;

import core.BeanDefinition;
import core.annotations.Component;
import core.annotations.ComponentScan;
import core.annotations.Configuration;
import core.context.BeanNameGenerator;
import core.context.ClassPathScanner;
import core.factory.SimpleBeanFactory;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * BFPP that bootstraps component scanning based on a primary source class.
 */
public class ComponentScanAnnotationFactoryPostProcessor implements BeanFactoryPostProcessor {

    private final Class<?> primarySource;
    private final ClassPathScanner scanner;

    public ComponentScanAnnotationFactoryPostProcessor(Class<?> primarySource, ClassLoader cl) {
        this.primarySource = Objects.requireNonNull(primarySource);
        this.scanner = new ClassPathScanner(cl);
    }

    @Override
    public void postProcessBeanFactory(SimpleBeanFactory beanFactory) {

        // check if primary source has @ComponentScan and @Configuration

        if (!primarySource.isAnnotationPresent(Configuration.class)
                || !primarySource.isAnnotationPresent(ComponentScan.class)) {
            // If you want strict Spring-like behavior, throw here instead.
            throw new IllegalArgumentException("Primary source must be @Configuration and @ComponentScan");
        }


        // extract all base packages to scan
        List<String> basePackages = determineBasePackages(primarySource);

        for (String pkg : basePackages) {
            Set<Class<?>> classes = scanner.scan(pkg);

            for (Class<?> c : classes) {

                // v1: only register @Component
                Component comp = c.getAnnotation(Component.class);
                if (comp == null) continue;

                if (c.isInterface()) continue;
                if (Modifier.isAbstract(c.getModifiers())) continue;

                String name = comp.value();
                if (name == null || name.isBlank()) {
                    name = BeanNameGenerator.defaultName(c);
                }

                if (beanFactory.containsBeanDefinition(name)) {
                    continue;
                }

                System.out.println("ComponentScan BFPP registering bean: " + name + " -> " + c.getName());

                beanFactory.registerBeanDefinition(name, new BeanDefinition(c));
            }
        }
    }

    private List<String> determineBasePackages(Class<?> source) {


        ComponentScan cs = source.getAnnotation(ComponentScan.class);


        LinkedHashSet<String> pkgs = new LinkedHashSet<>();

        // extract the basePackages attribute of the annotation
        // the attribute contains an array of package names to scan
        pkgs.addAll(Arrays.asList(cs.basePackages()));

        // print all extracted packages for debugging
        for (String p : pkgs) {
            System.out.println("ComponentScan BFPP will scan package: " + p);
        }


        return new ArrayList<>(pkgs);
    }

}
