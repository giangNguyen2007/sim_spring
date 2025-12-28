import core.context.SimpleApplicationContext;
import core.aop.proxy.JdkDynamicAopProxy;
import demo.aop.AopInterface;
import demo.service.UserService;

import java.lang.reflect.Proxy;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        // =============== CREATE APPLICATION CONTEXT ===============
        // scan the "demo" package for components
        SimpleApplicationContext appCtx = new SimpleApplicationContext("demo");

        // =============== RUN THE CONTEXT ===============

        // refresh context to initialize singleton beans
        appCtx.refresh();

        // =============== RETRIEVE BEANS AND USE IT ===============

        UserService srv = appCtx.getBean(UserService.class);

        String userName = srv.getUserName(42L);

        System.out.println("Found user name: " + userName);

        // close context to trigger destruction callbacks
        appCtx.close();


    }
}