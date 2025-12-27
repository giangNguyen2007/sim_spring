package core.annotations;


import java.lang.annotation.*;

/**
 * @PostConstruct
 *
 * gng : mini version of JSR-250 PostConstruct.
 * Called after dependency injection (populateBean),
 * and before init-method (BeanDefinition.initMethod).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostConstruct {
}
