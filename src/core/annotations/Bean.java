package core.annotations;

import java.lang.annotation.*;

/**
 * Marks a method on a @Configuration class as a bean factory method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bean {

    /**
     * Optional explicit bean name. If empty, default = method name.
     */
    String value();  // required
}
