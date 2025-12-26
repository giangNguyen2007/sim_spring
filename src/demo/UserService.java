package demo;

import core.annotations.Autowired;
import core.bpp.aware.BeanNameAware;

public class UserService implements BeanNameAware {

    private String beanName;
    @Autowired
    private UserRepository repo;

    public void init() {
        System.out.println("UserService init() => my bean name is: " + beanName);
    }

    public void destroy() {
        System.out.println("UserService destroy()");
    }

    public String getName(long id) {
        return repo.findUserName(id);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
