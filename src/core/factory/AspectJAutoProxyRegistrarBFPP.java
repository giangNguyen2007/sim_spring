package core.factory;

import core.aop.advisors.AdvisorInterface;
import core.aop.annotations.Aspect;
import core.bpp.AopAutoProxyCreatorPostProcessor;
import core.interfaces.BeanFactoryPostProcessor;

import java.util.List;

public class AspectJAutoProxyRegistrarBFPP implements BeanFactoryPostProcessor {

    private final AspectAdvisorFactory advisorFactory = new AspectAdvisorFactory();

    @Override
    public void postProcessBeanFactory(SimpleBeanFactory beanFactory) {
        // 1) Ensure AOP creator exists
        AopAutoProxyCreatorPostProcessor apc = beanFactory.getOrCreateAopAutoProxyCreator();

        // 2) find all names of all beans annotated with @Aspect
        List<String> aspectNames = beanFactory.findBeanNamesByAnnotation(Aspect.class);


        for (String aspectName : aspectNames) {
            // retrieve aspect bean
            Object aspectBean = beanFactory.getBean(aspectName);

            // build advisors from bean's methods annotated with @Around
            List<AdvisorInterface> advisors = advisorFactory.buildAdvisors(aspectBean);

            // register advisors with the aop post-processor
            for (AdvisorInterface advisor : advisors) {
                apc.addAdvisor(advisor);
            }
        }
    }
}
