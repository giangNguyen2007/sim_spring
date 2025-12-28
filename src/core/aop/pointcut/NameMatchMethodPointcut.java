package core.aop.pointcut;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple pointcut:
 * methodName : match by method name (e.g. "placeOrder", "pay").
 * className : contains filter for class name (optional, cheap).
 * You can later implement annotations, packages, regex, etc.
 */
public class NameMatchMethodPointcut implements PointCutInterface {

    // set of method names to match
    // any method that is in this set will be matched
    private final Set<String> methodNames = new HashSet<>();

    // any class that contains this string in its name will also be matched
    private final String filterClassNameString; // optional cheap class filter

    public NameMatchMethodPointcut(String filterClassNameString) {
        this.filterClassNameString = filterClassNameString;
    }

    public NameMatchMethodPointcut addMethodName(String name) {
        // add method name to the set
        this.methodNames.add(name);
        return this;
    }

    @Override
    public boolean matchesClass(Class<?> targetClass) {
        if (filterClassNameString == null || filterClassNameString.isBlank()) return true;

        // return true if class name contains the filter string


        return targetClass.getName().contains(filterClassNameString);
    }

    @Override
    public boolean matchesMethod(Method method, Class<?> targetClass) {

        // return true if method name is in the set
        return methodNames.contains(method.getName());
    }
}
