package core.aop.pointcut;


import core.aop.annotations.Transactional;

import java.lang.reflect.Method;

/**
 * Pointcut: matches if @Transactional exists on:
 * - method, OR
 * - target class
 */
public class AnnotationTransactionPointcut implements PointCutInterface {

    @Override
    public boolean matchesClass(Class<?> targetClass) {
        return targetClass.isAnnotationPresent(Transactional.class);
    }

    @Override
    public boolean matchesMethod(Method method, Class<?> targetClass) {
        // method-level annotation
        if (method.isAnnotationPresent(Transactional.class)) {
            System.out.println("[TX ANNOTATION POINTCUT ]Transactional method matched: " + method.getName());
            return true;
        }

        // class-level annotation
        return targetClass.isAnnotationPresent(Transactional.class);
    }
}