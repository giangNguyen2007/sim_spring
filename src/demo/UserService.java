package demo;

import core.annotations.Autowired;

public class UserService {

    @Autowired
    private UserRepository repo;

    public void init() {
        System.out.println("UserService init()");
    }

    public void destroy() {
        System.out.println("UserService destroy()");
    }

    public String getName(long id) {
        return repo.findUserName(id);
    }
}
