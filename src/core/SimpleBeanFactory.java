package core;


//This is the “engine” that:
//
//        stores definitions
//
//        creates beans
//
//        injects dependencies
//
//        caches singletons
//
//We start with constructor injection by type, picking the “best” constructor (we’ll keep it simple).

import core.annotations.Autowired;
import core.interfaces.BeanFactory;
import core.interfaces.BeanPostProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class SimpleBeanFactory implements BeanFactory {

    // main registry for bean definitions
    private final Map<String, BeanDefinition> beanDefinitionRegistry = new HashMap<>();

    // ==========================================================
    // Section 2) Singleton caches (Spring-like 3-level cache)
    // ==========================================================

    // Level 1: fully initialized singletons (the "real" singleton cache)
    private final Map<String, Object> singletonObjects_L1 = new HashMap<>();


    // Level 2: early-exposed singletons (partially created objects)
    // Used to break certain singleton cycles.
    private final Map<String, Object> earlySingletonObjects_L2 = new HashMap<>();

    // Level 3: singleton factories that can produce an early reference
    // In real Spring, this is where AOP proxies can be created early.
    private final Map<String, ObjectFactory<?>> singletonFactories_L3 = new HashMap<>();

    // Track which singletons are currently being created (cycle detection)
    private final Set<String> singletonsCurrentlyInCreation = new HashSet<>();

    // destruction callbacks
    private final Map<String, Runnable> destroyCallbacks = new LinkedHashMap<>();


    // ==========================================================
    // Section 2.5) BeanPostProcessors registry
    // ==========================================================
    // gng : Keep a list of post processors (AOP/proxy hooks)
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    // ========================= END OF FIELDS =========================

    /**
     * Minimal functional interface like Spring's ObjectFactory.
     * Used so we can expose an early reference lazily (factory invoked only if needed).
     */
    @FunctionalInterface
    private interface ObjectFactory<T> {
        T getObject();
    }


    // ==========================================================
    // Section 3) BeanDefinition registration and BeanPostProcessor API
    // ==========================================================
    @Override
    public void registerBeanDefinition(String name, BeanDefinition def) {
        // objectif : registrer une definition de bean


        if (beanDefinitionRegistry.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate bean name: " + name);
        }
        beanDefinitionRegistry.put(name, def);
    }


    /**
     * Auto-detect and register all BeanPostProcessor beans.
     *
     * IMPORTANT:
     * - must run BEFORE preInstantiateSingletons()
     * - otherwise BPP will NOT apply to eagerly-created singletons
     */
    public void registerBeanPostProcessors() {
        List<String> bppNames = new ArrayList<>();

        // Find all beans that is a BeanPostProcessor
        for (var e : beanDefinitionRegistry.entrySet()) {
            if (BeanPostProcessor.class.isAssignableFrom(e.getValue().getBeanClass())) {
                bppNames.add(e.getKey());
            }
        }

        // Instantiate and register them
        for (String name : bppNames) {
            Object bean = getBean(name);
            this.addBeanPostProcessor((BeanPostProcessor) bean);
        }
    }

    /**
     * Register a BeanPostProcessor manually (optional helper).
     *
     * In a real container, BPPs are usually registered during refresh().
     */
    public void addBeanPostProcessor(BeanPostProcessor bpp) {
        if (bpp == null) throw new IllegalArgumentException("bpp must not be null");
        this.beanPostProcessors.add(bpp);
    }


    // ==========================================================
    // Section 4) Public API - getBean by name / type
    // ==========================================================

    @Override
    // get bean by name
    public Object getBean(String name) {

        BeanDefinition def = beanDefinitionRegistry.get(name);
        if (def == null) throw new NoSuchElementException("No bean named: " + name);

        // if singleton
        if (def.isSingleton()) {
            // First: attempt to return from caches (L1/L2/L3)
            Object singleton = getSingletonFromCaches(name, true);
            if (singleton != null) return singleton;

            // If singleton not found in caches.
            // If it's already in creation, this is typically a constructor-cycle that we cannot resolve.
            if (isCurrentlyInCreation(name)) {
                throw new IllegalStateException(
                        "Circular dependency detected while creating singleton '" + name + "'. " +
                                "Early references can only help if at least one side can be instantiated " +
                                "before dependency injection completes (e.g., setter/field injection). " +
                                "With pure constructor injection, this cycle is not resolvable."
                );
            }

            // if not found in any cache and not currently in creation
            // Create it (createBean will publish it into L1 itself)
            return createBean(name, def);
        }


        // if not singleton => prototype
        // Prototype: always create a new instance, no singleton caches.
        return createBean(name, def);
    }

    @Override
    // get bean by type
    public <T> T getBean(Class<T> type) {
        // If exactly one matching bean exists, return it. Otherwise error.
        List<String> matches = new ArrayList<>();

        // definition.entrySet() => set of BeanDefinition object
        for (var e : beanDefinitionRegistry.entrySet()) {
            if (type.isAssignableFrom(e.getValue().getBeanClass())) {
                matches.add(e.getKey());
            }
        }

        // if not match or multiple matches => exception
        if (matches.isEmpty()) throw new NoSuchElementException("No bean of type: " + type.getName());
        if (matches.size() > 1) throw new IllegalStateException("Multiple beans of type: " + type.getName() + " => " + matches);

        return type.cast(getBean(matches.get(0)));
    }
    // => call getBean(name) for each singleton definition

    public void preInstantiateSingletons() {
        for (var e : beanDefinitionRegistry.entrySet()) {
            if (e.getValue().isSingleton()) {
                getBean(e.getKey());
            }
        }
    }

    public void close() {
        // destroy in reverse registration order like a stack (simple approach)
        List<Runnable> callbacks = new ArrayList<>(destroyCallbacks.values());
        Collections.reverse(callbacks);
        for (Runnable r : callbacks) r.run();
    }

    // ======= PRIVATE METHODS BELOW =======


    // ==========================================================
    // Section 6) Core singleton cache logic (3-level cache)
    // ==========================================================

    /**
     * Returns singleton from:
     *   L1: singletonObjects
     *   L2: earlySingletonObjects (only if bean is currently in creation)
     *   L3: singletonFactories (if allowEarlyReference)
     *
     * This mimics Spring's logic:
     *   - L1 hit => done
     *   - if currently in creation, allow returning early ref from L2/L3 to break cycles
     */
    private Object getSingletonFromCaches(String name, boolean allowEarlyReference) {

        // query L1: fully created singleton
        Object singleton = singletonObjects_L1.get(name);
        if (singleton != null) return singleton;

        // Only attempt early reference if the bean is currently in creation
        if (!isCurrentlyInCreation(name)) return null;

        // if not found in L1, check L2
        // 2) L2: early singleton object
        Object early = earlySingletonObjects_L2.get(name);
        if (early != null) return early;

        // if not found in L2, check L3
        // 3) L3: factory producing early reference (usually for proxies)
        if (allowEarlyReference) {
            ObjectFactory<?> factory = singletonFactories_L3.get(name);
            if (factory != null) {
                Object earlyRef = factory.getObject();

                // Once created, promote it to L2 and remove L3 factory
                earlySingletonObjects_L2.put(name, earlyRef);
                singletonFactories_L3.remove(name);
                return earlyRef;
            }
        }

        return null;
    }


    private boolean isCurrentlyInCreation(String name) {
        return singletonsCurrentlyInCreation.contains(name);
    }


    /**
     * Hook point: in real Spring this may return a proxy instead of the raw bean.
     * For now, we just return the instance itself.
     */


    /**
     *
     * gng : We now route this through BeanPostProcessors.getEarlyBeanReference(...)
     * so a processor can expose a proxy EARLY (important for circular references).
     */
    protected Object getEarlyBeanReference(String name, Object bean) {
        // In real Spring, this is where AOP proxies may be created early.
        // Here, we delegate to BeanPostProcessors.
        Object exposed = bean;
        for (BeanPostProcessor bpp : beanPostProcessors) {

            // return either the same bean or a proxy/wrapper
            exposed = bpp.getEarlyBeanReference(exposed, name);
            if (exposed == null) {
                throw new IllegalStateException(
                        "BeanPostProcessor '" + bpp.getClass().getName() + "' returned null from getEarlyBeanReference for bean '" + name + "'"
                );
            }
        }
        return exposed;
    }

    // ==========================================================
    // Section 7) Bean creation pipeline
    // ==========================================================

    private Object createBean(String name, BeanDefinition def) {
        boolean isSingleton = def.isSingleton();

        if (isSingleton) {
            // Mark "currently in creation" BEFORE instantiation
            beforeSingletonCreation(name);
        }

        try {
            // --------------------------------------------
            // Step 1) Instantiate
            // --------------------------------------------

            // instance is created with the constructor only
            // dependencies are not injected yet
            Object instance = (def.getSupplier() != null)
                    ? def.getSupplier().get()
                    : instantiateWithNoArgConstructor(def.getBeanClass());

            // -------------------------
            // Phase 2) Early exposure (singleton only)
            // -------------------------
            // This is the KEY for circular references: other beans can get an early ref
            // to this instance before it's fully populated.
            if (isSingleton) {
                singletonFactories_L3.put(name, () -> getEarlyBeanReference(name, instance));
            }

            // -------------------------
            // Phase 3) Populate dependencies ✅
            // -------------------------
            // This is what makes the 3-level cache "useful":
            // A and B can be instantiated first, then inject early refs during populate.
            // gng : inject dependencies (field + setter injection)
            this.populateBean(name, instance);

            // --------------------------------------------
            // Step 3) BeanPostProcessor - before init
            // --------------------------------------------
            // gng : BPP can validate or wrap the bean before init.
            Object exposedObject = applyBeanPostProcessorsBeforeInitialization(instance, name);


            // --------------------------------------------
            // Step 3) Init callbacks
            // --------------------------------------------
            this.invokeInit(exposedObject, def);


            // --------------------------------------------
            // Step 5) BeanPostProcessor - after init (AOP proxy hook)
            // --------------------------------------------
            exposedObject = applyBeanPostProcessorsAfterInitialization(exposedObject, name);

            // --------------------------------------------
            // Step 6) Register destroy callback (if any)
            // --------------------------------------------
            // Spring usually registers this for singletons, but you can keep it for all scopes.
            this.registerDestroyCallbackIfAny(name, exposedObject, def);

            // --------------------------------------------
            // Step 7) Publish singleton (move to L1)
            // --------------------------------------------
            if (isSingleton) {
                this.addSingletonToL1(name, exposedObject);
            }

            return exposedObject;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create bean '" + name + "' of type " + def.getBeanClass().getName(), e
            );
        } finally {
            if (isSingleton) {
                afterSingletonCreation(name);
            }
        }
    }

    private void beforeSingletonCreation(String name) {

        // Check if the bean is already in creation
        if (singletonsCurrentlyInCreation.contains(name)) {
            throw new IllegalStateException(
                    "Bean '" + name + "' is already in creation (circular reference)."
            );
        }

        // Mark the bean as currently in creation
        singletonsCurrentlyInCreation.add(name);
    }

    private void afterSingletonCreation(String name) {
        singletonsCurrentlyInCreation.remove(name);
    }

    /**
     * Publish fully initialized singleton to L1 and clear early caches.
     */
    private void addSingletonToL1(String name, Object singletonObject) {
        singletonObjects_L1.put(name, singletonObject);

        // Once fully created, we must remove any stale early refs/factories.
        earlySingletonObjects_L2.remove(name);
        singletonFactories_L3.remove(name);
    }


    // ==========================================================
    // Section 8) Instantiate strategy
    // ==========================================================
    private Object instantiateWithNoArgConstructor(Class<?> clazz) throws Exception {
        // ✅ IMPORTANT tweak for circular deps:
        // Prefer no-arg constructor if present, because it allows instantiation
        // without needing other beans immediately (constructor cycles are the hardest).
        Constructor<?> noArg = findNoArgConstructor(clazz);
        if (noArg != null) {
            noArg.setAccessible(true);
            return noArg.newInstance();
        }

        // Fallback: your original greedy constructor injection.
        // Note: this may still fail for pure constructor cycles (A(B), B(A)).
        return instantiateWithConstructorInjection(clazz);
    }

    private Constructor<?> findNoArgConstructor(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Object instantiateWithConstructorInjection(Class<?> clazz) throws Exception {
        Constructor<?>[] ctors = clazz.getDeclaredConstructors();

        // pick the constructor with most parameters
        Constructor<?> chosen = chooseConstructor(ctors);
        chosen.setAccessible(true);

        // resolve each constructor parameter by type
        Class<?>[] paramTypes = chosen.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            // Note: this may re-enter getBean() and trigger circular dependency paths.
            args[i] = getBean(paramTypes[i]);
        }

        return chosen.newInstance(args);
    }

    private Constructor<?> chooseConstructor(Constructor<?>[] ctors) {
        // Simple rule: pick the constructor with the most parameters
        Constructor<?> best = ctors[0];
        for (Constructor<?> c : ctors) {
            if (c.getParameterCount() > best.getParameterCount()) best = c;
        }
        return best;
    }

    // ==========================================================
    // Section 9) Populate phase (field + setter injection)
    // ==========================================================
    private void populateBean(String beanName, Object bean) {
        Class<?> clazz = bean.getClass();

        // -------- Field injection --------
        for (Field f : clazz.getDeclaredFields()) {


            // extract @Autowired on the field  if exist
            Autowired aw = f.getAnnotation(Autowired.class);
            if (aw == null) continue;

            // skip static/final fields
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) continue;

            // resolve dependency for the field type => get the object of the target type
            Object dep;
            try {
                dep = resolveDependency(f.getType(), aw.required(), beanName, "field " + f.getName());
            } catch (RuntimeException ex) {
                throw ex;
            }

            try {
                f.setAccessible(true);

                // inject the resolved dependency into the field via reflection
                f.set(bean, dep);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to inject " + f + " into bean '" + beanName + "'", e);
            }
        }


        // -------- Setter injection --------
        for (Method m : clazz.getDeclaredMethods()) {
        Autowired aw = m.getAnnotation(Autowired.class);
        if (aw == null) continue;

        if (m.getParameterCount() != 1) {
            throw new IllegalStateException("@Autowired method must have exactly 1 parameter: " + m);
        }

        Class<?> depType = m.getParameterTypes()[0];
        Object dep = resolveDependency(depType, aw.required(), beanName, "method " + m.getName());

        try {
            m.setAccessible(true);
            m.invoke(bean, dep);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject via " + m + " into bean '" + beanName + "'", e);
        }
    }
}

    private Object resolveDependency(Class<?> depType, boolean required, String requestingBean, String injectionPoint) {
        try {
            return getBean(depType); // will use early refs if needed
        } catch (RuntimeException ex) {
            if (!required) return null;
            throw new RuntimeException(
                    "Unsatisfied dependency for bean '" + requestingBean + "' at " + injectionPoint +
                            " (type: " + depType.getName() + ")", ex
            );
        }
    }


    // ==========================================================
    // Section 9.5) Apply BeanPostProcessors
    // ==========================================================

    private Object applyBeanPostProcessorsBeforeInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor bpp : beanPostProcessors) {
            result = bpp.postProcessBeforeInitialization(result, beanName);
            if (result == null) {
                throw new IllegalStateException(
                        "BeanPostProcessor '" + bpp.getClass().getName() + "' returned null from postProcessBeforeInitialization for bean '" + beanName + "'"
                );
            }
        }
        return result;
    }

    private Object applyBeanPostProcessorsAfterInitialization(Object bean, String beanName) {
        Object result = bean;
        for (BeanPostProcessor bpp : beanPostProcessors) {
            result = bpp.postProcessAfterInitialization(result, beanName);
            if (result == null) {
                throw new IllegalStateException(
                        "BeanPostProcessor '" + bpp.getClass().getName() + "' returned null from postProcessAfterInitialization for bean '" + beanName + "'"
                );
            }
        }
        return result;
    }


    // ==========================================================
    // Section 10) init / destroy
    // ==========================================================
    private void invokeInit(Object instance, BeanDefinition def) throws Exception {
        String init = def.getInitMethod();
        if (init == null || init.isBlank()) return;

        Method m = instance.getClass().getMethod(init);
        m.setAccessible(true);
        m.invoke(instance);
    }

    private void registerDestroyCallbackIfAny(String name, Object instance, BeanDefinition def) {
        String destroy = def.getDestroyMethod();
        if (destroy == null || destroy.isBlank()) return;

        destroyCallbacks.put(name, () -> {
            try {
                Method m = instance.getClass().getMethod(destroy);
                m.setAccessible(true);
                m.invoke(instance);
            } catch (Exception e) {
                throw new RuntimeException("Destroy method failed for bean: " + name, e);
            }
        });
    }



}
