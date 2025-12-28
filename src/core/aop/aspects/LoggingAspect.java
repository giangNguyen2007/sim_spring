package core.aop.aspects;


import core.aop.annotations.Around;
import core.aop.annotations.Aspect;
import core.aop.joinpoint.ProceedingJoinPoint;

@Aspect
public class LoggingAspect {

    @Around("execution:Service")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {

        System.out.println("[ASP] -> " + pjp.getMethod().getName());
        try {
            Object out = pjp.proceed();
            System.out.println("[ASP] <- " + pjp.getMethod().getName());
            return out;
        } catch (Throwable ex) {
            System.out.println("[ASP] !! " + ex);
            throw ex;
        }
    }
}
