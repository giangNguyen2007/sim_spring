package core.bpp.common_annotations;

import core.annotations.PostConstruct;
import core.interfaces.BeanPostProcessorInterface;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * CommonAnnotationBeanPostProcessor (built-in)
 *
 * gng : supports @PostConstruct.
 * - runs after populateBean (DI is done)
 * - runs before init-method (BeanDefinition.initMethod)
 *
 * NOTE: @PreDestroy is handled during container close (destruction phase),
 * not here (because BPP is about init lifecycle).
 */
public class CommonAnnotationBeanPostProcessor implements BeanPostProcessorInterface {

    // exposed API methods
    @Override
    // to be called to process bean
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        invokePostConstruct(bean, beanName);
        return bean;
    }

    // ================== INNER HELPERS ==================

    private void invokePostConstruct(Object bean, String beanName) {

        // find all methods annotated with @PostConstruct
        List<Method> methods = findAnnotatedNoArgMethods(bean.getClass(), PostConstruct.class);

        // invoke them in order
        for (Method m : methods) {
            // skip static methods
            if (Modifier.isStatic(m.getModifiers())) {
                throw new IllegalStateException("@PostConstruct method must not be static: " + m);
            }
            if (m.getParameterCount() != 0) {
                throw new IllegalStateException("@PostConstruct method must have no-args: " + m);
            }

            try {
                m.setAccessible(true);

                // invove the method
                m.invoke(bean);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to invoke @PostConstruct method " + m.getName() +
                                " on bean '" + beanName + "' (" + bean.getClass().getName() + ")", e
                );
            }
        }
    }

    /**
     * Find annotated methods in a deterministic order:
     * - superclass -> subclass
     * - within each class: declared methods order (JVM keeps it usually, not guaranteed)
     */
    private List<Method> findAnnotatedNoArgMethods(Class<?> beanClass, Class<?> annotationType) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> c = beanClass;

        // retrieve all the parent classes of the bean class
        while (c != null && c != Object.class) {
            hierarchy.add(c);
            c = c.getSuperclass();
        }
        Collections.reverse(hierarchy); // superclass first

        List<Method> result = new ArrayList<>();

        // scan each class for annotated methods
        // and collect them in results[]
        for (Class<?> clazz : hierarchy) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getAnnotation((Class) annotationType) != null) {
                    result.add(m);
                }
            }
        }
        return result;
    }
}
