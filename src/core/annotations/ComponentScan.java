package core.annotations;

import java.lang.annotation.*;

/**
 * Minimal Spring-like annotation.
 *
 * v1 rules:
 * - if basePackages is empty, default = package of the annotated class
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentScan {

    /**
     * Packages to scan, e.g. {"com.demo", "com.foo"}
     */
    String[] basePackages() default {};


}
