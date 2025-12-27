package core.annotations;

import java.lang.annotation.*;

/**
 * @PreDestroy
 *
 * gng : mini version of JSR-250 PreDestroy.
 * Called when the context is closing, before destroy-method
 * (BeanDefinition.destroyMethod).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreDestroy {
}
