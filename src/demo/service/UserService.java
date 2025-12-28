package demo.service;

import core.annotations.Autowired;
import core.annotations.Component;
import core.bpp.aware.BeanNameAware;
import demo.repo.UserRepository;

@Component
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

    public String getUserName(long id) {
        return repo.findUserName(id);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }


    public UserRepository getRepo() {
        return repo;
    }
}
