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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class SimpleBeanFactory implements BeanFactory {

    private final Map<String, BeanDefinition> definitions = new HashMap<>();
    private final Map<String, Object> singletonObjects = new HashMap<>();
    private final Map<String, Runnable> destroyCallbacks = new LinkedHashMap<>();


    //
    @Override
    public void registerBeanDefinition(String name, BeanDefinition def) {
        // objectif : registrer une definition de bean


        if (definitions.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate bean name: " + name);
        }
        definitions.put(name, def);
    }

    @Override
    // get bean by name
    public Object getBean(String name) {


        BeanDefinition def = definitions.get(name);
        if (def == null) throw new NoSuchElementException("No bean named: " + name);

        if (def.isSingleton()) {
            // if singleton, check cache first
            if (singletonObjects.containsKey(name)) {

                return singletonObjects.get(name);

            } else {
                Object bean = createBean(name, def);
                singletonObjects.put(name, bean);
                return bean;
            }

        }
        return createBean(name, def); // prototype: always new
    }

    @Override
    // get bean by type
    public <T> T getBean(Class<T> type) {
        // If exactly one matching bean exists, return it. Otherwise error.
        List<String> matches = new ArrayList<>();

        // definition.entrySet() => set of BeanDefinition object
        for (var e : definitions.entrySet()) {
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
        for (var e : definitions.entrySet()) {
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


    // ============== Helper methods level 1 ==================
    private Object createBean(String name, BeanDefinition def) {
        try {
            Object instance = (def.getSupplier() != null)
                    
                    // if supplier is provided, use it
                    ? def.getSupplier().get()
                    
                    // else use constructor injection
                    : instantiateWithConstructorInjection(def.getBeanClass());

            invokeInit(instance, def);
            registerDestroyCallbackIfAny(name, instance, def);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean '" + name + "' of type " + def.getBeanClass().getName(), e);
        }
    }
    // => call instantiateWithConstructorInjection(clazz)
    // => call invokeInit(instance, def)
    // => call registerDestroyCallbackIfAny(name, instance, def)


    // ============== Helper methods level 2 ==================

    private Object instantiateWithConstructorInjection(Class<?> clazz) throws Exception {
        // get list of constructors defined in the class
        Constructor<?>[] ctors = clazz.getDeclaredConstructors();
        
        // get the constructor to use => one with most parameters
        Constructor<?> chosen = chooseConstructor(ctors);

        chosen.setAccessible(true);

        // for each parameter, resolve dependency by type
        Class<?>[] paramTypes = chosen.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = getBean(paramTypes[i]); // by-type resolution
        }

        // invoke constructor with resolved args
        return chosen.newInstance(args);
    }

    private Constructor<?> chooseConstructor(Constructor<?>[] ctors) {
        // Simple rule: pick the constructor with the most parameters
        // (Spring has more complex rules; we start simple)
        Constructor<?> best = ctors[0];
        for (Constructor<?> c : ctors) {
            if (c.getParameterCount() > best.getParameterCount()) best = c;
        }
        return best;
    }

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
