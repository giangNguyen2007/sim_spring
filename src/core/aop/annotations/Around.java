package core.aop.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Around {
    /**
     * We keep it mini: a simple string expression.
     * Example: "execution:com.demo.service.*.*"  or "execution:*..service..*.*"
     */
    String value();
}