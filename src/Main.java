import core.BeanDefinition;
import core.SimpleApplicationContext;
import core.aop.proxy.JdkDynamicAopProxy;
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

        // register UserRepository bean
        appCtx.registerBean("aopService", new BeanDefinition(AopService.class));

        appCtx.registerBean("aopTransactionService", new BeanDefinition(AopTransactionService.class));

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

        // invoke method with @Transactional
        AopTransactionInterface txBean = appCtx.getBean(AopTransactionInterface.class);

        if (Proxy.isProxyClass(txBean.getClass())) {
            Object handler = Proxy.getInvocationHandler(txBean);
            System.out.println("handler = " + handler.getClass().getName());

            boolean isOurAop2 = handler instanceof JdkDynamicAopProxy;
            System.out.println("isOurAop=" + isOurAop2);
        }
        txBean.processTransaction(50);

        // close context to trigger destruction callbacks
        appCtx.close();


    }
}