package core.bpp.common_annotations;


import core.annotations.PreDestroy;
import core.interfaces.DestructionAwareBeanPostProcessorInterface;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PreDestroyBeanPostProcessor (built-in)
 *
 * gng : Dedicated processor ONLY for @PreDestroy.
 *
 * Mechanism:
 * - runs during destruction phase (context close)
 * - via DestructionAwareBeanPostProcessor.postProcessBeforeDestruction(...)
 */
public class PreDestroyBeanPostProcessor implements DestructionAwareBeanPostProcessorInterface {

    @Override
    public boolean requiresDestruction(Object bean) {
        // gng : we only ask the factory to register destruction callback
        // if there is at least one @PreDestroy method.
        return hasAnnotatedMethod(bean.getClass(), PreDestroy.class);
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        invokePreDestroy(bean, beanName);
    }


    // ================== INNER HELPERS ==================

    private void invokePreDestroy(Object bean, String beanName) {

        // find all methods annotated with @PreDestroy
        List<Method> methods = findAnnotatedMethods(bean.getClass(), PreDestroy.class);

        for (Method m : methods) {
            if (Modifier.isStatic(m.getModifiers())) {
                throw new IllegalStateException("@PreDestroy method must not be static: " + m);
            }
            if (m.getParameterCount() != 0) {
                throw new IllegalStateException("@PreDestroy method must have no-args: " + m);
            }

            try {
                m.setAccessible(true);

                System.out.println("[PreDestroyBeanPostProcessor] : Invoking @PreDestroy method: " + m.getName() + " on bean: " + beanName);
                m.invoke(bean);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to invoke @PreDestroy method '" + m.getName() +
                                "' on bean '" + beanName + "' (" + bean.getClass().getName() + ")", e
                );
            }
        }
    }

    private boolean hasAnnotatedMethod(Class<?> beanClass, Class<PreDestroy> ann) {
        return !findAnnotatedMethods(beanClass, ann).isEmpty();
    }

    private List<Method> findAnnotatedMethods(Class<?> beanClass, Class<PreDestroy> ann) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> c = beanClass;

        while (c != null && c != Object.class) {
            hierarchy.add(c);
            c = c.getSuperclass();
        }
        Collections.reverse(hierarchy); // superclass first

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
