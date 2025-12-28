package core.aop.annotations;

import java.lang.annotation.*;

/**
 * Marks a method (or class) as transactional.
 *
 * Keep it minimal for now.
 * Later you can add:
 * - propagation, isolation, readOnly, rollbackFor, noRollbackFor...
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Transactional {
}
