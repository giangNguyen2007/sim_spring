package core.bpp.common_annotations;

import core.annotations.PostConstruct;
import core.bpp.interfaces.BeanPostProcessorInterface;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PostConstructBeanPostProcessor (built-in)
 *
 * gng : Dedicated processor ONLY for @PostConstruct.
 *
 * Mechanism:
 * - runs in postProcessBeforeInitialization()
 * - so it happens AFTER populateBean (DI done)
 * - and BEFORE BeanDefinition.initMethod (if any)
 */
public class PostConstructBeanPostProcessor implements BeanPostProcessorInterface {

    //=================== EXPOSED API METHODS ==================
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        invokePostConstruct(bean, beanName);
        return bean;
    }

    // ================== INNER HELPERS ==================
    private void invokePostConstruct(Object bean, String beanName) {

        // find all methods annotated with @PostConstruct
        List<Method> methods = findAnnotatedMethods(bean.getClass(), PostConstruct.class);

        for (Method m : methods) {
            // validations (fail-fast)
            if (Modifier.isStatic(m.getModifiers())) {
                throw new IllegalStateException("@PostConstruct method must not be static: " + m);
            }
            if (m.getParameterCount() != 0) {
                throw new IllegalStateException("@PostConstruct method must have no-args: " + m);
            }

            try {
                m.setAccessible(true);
                m.invoke(bean);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to invoke @PostConstruct method '" + m.getName() +
                                "' on bean '" + beanName + "' (" + bean.getClass().getName() + ")", e
                );
            }
        }
    }

    /**
     * Deterministic-ish order:
     * - superclass -> subclass
     * - then declared methods per class
     */
    private List<Method> findAnnotatedMethods(Class<?> beanClass, Class<PostConstruct> ann) {

        List<Class<?>> hierarchy = new ArrayList<>();

        // retrieve all the parent classes of the bean class
        Class<?> c = beanClass;

        while (c != null && c != Object.class) {
            hierarchy.add(c);
            c = c.getSuperclass();
        }
        Collections.reverse(hierarchy); // superclass first

        // scan each class for annotated methods
        // and collect them in results[]

        List<Method> result = new ArrayList<>();
        for (Class<?> clazz : hierarchy) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getAnnotation(ann) != null) {
                    result.add(m);
                }
            }
        }
        return result;
    }
}
