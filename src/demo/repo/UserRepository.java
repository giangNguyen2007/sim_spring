package demo.repo;

import core.annotations.Autowired;
import core.annotations.Component;
import demo.service.UserService;

@Component
public class UserRepository {

    public String findUserName(long id) {
        return "user-" + id;
    }
}