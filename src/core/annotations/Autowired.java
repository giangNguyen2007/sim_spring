package core.annotations;

import java.lang.annotation.*;

/**
 * Minimal @Autowired annotation for our container.
 * We will support:
 *  - field injection
 *  - setter injection (methods)
 *
 * This is what makes early references useful, because:
 *   instantiate(A) -> expose early A -> populate(A needs B) -> populate(B needs early A) ...
 */
// apply at runtime
@Retention(RetentionPolicy.RUNTIME)
// can be applied to class fields and methods
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Autowired {
    boolean required() default true;
}
