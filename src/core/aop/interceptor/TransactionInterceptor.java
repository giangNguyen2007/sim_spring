package core.aop.interceptor;

import core.aop.invocation.MethodInvocationInterface;
import core.aop.transaction.TransactionManagerInterface;

/**
 * Transaction around advice.
 *
 * This is a classic AOP pattern:
 *  - begin
 *  - proceed
 *  - commit
 *  - rollback on exception
 */
public class TransactionInterceptor implements MethodInterceptorInterface {

    private final TransactionManagerInterface transactionManager;

    public TransactionInterceptor(TransactionManagerInterface transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(MethodInvocationInterface invocation) throws Throwable {

        TransactionManagerInterface.TransactionStatus status = transactionManager.begin();

        try {
            Object result = invocation.proceed();
            transactionManager.commit(status);
            return result;
        } catch (Throwable ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }
}
