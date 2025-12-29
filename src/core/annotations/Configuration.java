package core.annotations;

import java.lang.annotation.*;

/**
 * Minimal configuration marker.
 *
 * v1 semantics in this mini-spring:
 * - It does NOT automatically register itself as a bean.
 * - It is used as a "primary source" type that can carry @ComponentScan.
 *
 * Later (v2/v3):
 * - treat @Configuration as @Component (so config class is a bean)
 * - parse @Bean methods into BeanDefinitions
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {
}
