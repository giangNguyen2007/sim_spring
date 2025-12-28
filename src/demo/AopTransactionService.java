package demo;

import core.aop.annotations.Transactional;

public class AopTransactionService implements AopTransactionInterface {


    @Override
    @Transactional
    public void processTransaction(int amount) {
        System.out.println("Process Transaction :" + amount);
    }

}
