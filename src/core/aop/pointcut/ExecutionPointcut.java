package core.aop.pointcut;

import java.lang.reflect.Method;

/**
 * Mini execution pointcut.
 *
 * Supported examples (define your own rules):
 * - "execution:com.demo.service.*.*"  => classes in package com.demo.service, any class, any method
 * - "execution:com.demo..service..*.*" => allow ".." wildcard for subpackages
 *
 * Implementation can start naive: string contains / startsWith checks.
 */
public class ExecutionPointcut implements PointCutInterface {

    // pattern extracted from @Around value
    // after "execution:"
    // example @Around("execution:core.demo.service.*.*")
    // pattern = "com.demo.service.*.*"
    private final String pattern;

    public ExecutionPointcut(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean matchesClass(Class<?> targetClass) {
        // Minimal: check FQN against pattern with '*' and '..'
        String name = targetClass.getName();
//        return SimplePatternMatcher.match(pattern, name);
        // Very minimal implementation: check if pattern is contained in class name
          return name.contains(pattern);
    }

    @Override
    public boolean matchesMethod(Method method, Class<?> targetClass) {
        // Minimal: if class matches, allow all methods.
        // Next iteration: include method name pattern.
        return true;
    }
}
