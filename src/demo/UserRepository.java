package demo;

import core.annotations.Autowired;

public class UserRepository {

    @Autowired
    private UserService service;
    public String findUserName(long id) {
        return "user-" + id;
    }
}