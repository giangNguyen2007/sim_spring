package core;

import java.util.function.Supplier;

public class BeanDefinition {
    private final Class<?> beanClass;
    private final String scope; // "singleton" or "prototype"


    // two bean creation strategies:
    // default: instantiate by reflection from beanClass
    // optional: a Supplier<?> for factory-style beans
    private final Supplier<?> supplier; // optional factory

    // lifecycle methods
    private String initMethod;  // => name of the initialization method defined in the bean class
    private String destroyMethod; // => name of the destruction method defined in the bean class


    // ============== Constructors ==================

    public BeanDefinition(Class<?> beanClass) {
        this(beanClass, "singleton", null);
    }

    public BeanDefinition(Class<?> beanClass, String scope, Supplier<?> supplier) {
        this.beanClass = beanClass;
        this.scope = scope == null ? "singleton" : scope;
        this.supplier = supplier;
    }

    // =============== Getters and Setters ==================
    public Class<?> getBeanClass() { return beanClass; }
    public String getScope() { return scope; }
    public boolean isSingleton() { return "singleton".equals(scope); }
    public Supplier<?> getSupplier() { return supplier; }

    public String getInitMethod() { return initMethod; }
    public void setInitMethod(String initMethod) { this.initMethod = initMethod; }

    public String getDestroyMethod() { return destroyMethod; }
    public void setDestroyMethod(String destroyMethod) { this.destroyMethod = destroyMethod; }
}
