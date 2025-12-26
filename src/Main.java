import core.BeanDefinition;
import core.SimpleApplicationContext;
import demo.UserRepository;
import demo.UserService;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        SimpleApplicationContext ctx = new SimpleApplicationContext();

        // register UserRepository bean
        ctx.registerBean("userRepository", new BeanDefinition(UserRepository.class));


        // register UserService bean with init and destroy methods
        BeanDefinition userServiceDef = new BeanDefinition(UserService.class);
        userServiceDef.setInitMethod("init");
        userServiceDef.setDestroyMethod("destroy");

        ctx.registerBean("userService", userServiceDef);


        // refresh context to initialize singleton beans
        ctx.refresh();

        UserService service = ctx.getBean(UserService.class);
        System.out.println(service.getName(42));

        ctx.close();


    }
}