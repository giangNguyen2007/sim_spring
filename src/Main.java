import core.BeanDefinition;
import core.SimpleApplicationContext;
import core.aop.aspects.LoggingAspect;
import core.aop.proxy.JdkDynamicAopProxy;
import core.factory.AspectJAutoProxyRegistrarBFPP;
import demo.AopInterface;
import demo.AopService;
import demo.AopTransactionInterface;
import demo.AopTransactionService;

import java.lang.reflect.Proxy;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        SimpleApplicationContext appCtx = new SimpleApplicationContext();

        // ============== REGISTER BEAN DEFINITIONS ===============

        // register aspect bean
        // annotated with @Aspect => will be processed by AspectJAutoProxyRegistrarBFPP
        // loggingAspect => log Around for classes with name containing "Service"
        appCtx.registerBean("loggingAspect", new BeanDefinition(LoggingAspect.class));

        // register UserRepository bean

        appCtx.registerBean("aopService", new BeanDefinition(AopService.class));


        // ================== Register Factory Post Processors ==================
        // Add BFPP that generates advisors from @Aspect beans
        appCtx.addBeanFactoryPostProcessor(new AspectJAutoProxyRegistrarBFPP());

        // =============== RUN THE CONTEXT ===============

        // refresh context to initialize singleton beans
        appCtx.refresh();

        // =============== RETRIEVE BEANS AND USE IT ===============

        AopInterface bean = appCtx.getBean(AopInterface.class);

        if (Proxy.isProxyClass(bean.getClass())) {
            Object handler = Proxy.getInvocationHandler(bean);
            System.out.println("handler = " + handler.getClass().getName());

            boolean isOurAop = handler instanceof JdkDynamicAopProxy;
            System.out.println("isOurAop=" + isOurAop);
        }

        bean.pay(100);

        // close context to trigger destruction callbacks
        appCtx.close();


    }
}