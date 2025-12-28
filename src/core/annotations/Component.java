package core.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    /**
     * Optional bean name.
     * If empty, the container generates a default name.
     */
    String value() default "";
}
