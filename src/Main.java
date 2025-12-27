import core.BeanDefinition;
import core.SimpleApplicationContext;
import demo.LifecycleDemoService;
import demo.UserRepository;
import demo.UserService;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        SimpleApplicationContext appCtx = new SimpleApplicationContext();

        // ============== REGISTER BEAN DEFINITIONS ===============

        // register UserRepository bean
        appCtx.registerBean("userRepository", new BeanDefinition(UserRepository.class));


        // register UserService bean with init and destroy methods
        BeanDefinition userServiceDef = new BeanDefinition(UserService.class);
        userServiceDef.setInitMethod("init");
        userServiceDef.setDestroyMethod("destroy");

        appCtx.registerBean("userService", userServiceDef);

        // register LifeCycleDemoService bean with init and destroy methods
        BeanDefinition demoServiceDef = new BeanDefinition(LifecycleDemoService.class);
        demoServiceDef.setInitMethod("init");
        demoServiceDef.setDestroyMethod("destroy");

        appCtx.registerBean("lifecycleDemoService", demoServiceDef);

        // =============== RUN THE CONTEXT ===============


        // refresh context to initialize singleton beans
        appCtx.refresh();

        UserService service = appCtx.getBean(UserService.class);
        System.out.println(service.getName(42));

        // close context to trigger destruction callbacks
        appCtx.close();


    }
}