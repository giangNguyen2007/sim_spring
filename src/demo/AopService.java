package demo;

public class AopService implements AopInterface {

    // method to be intercepted by AOP proxy
    public void pay(int amount) {
        System.out.println("pay method called with amount :" + amount);
    }


}
