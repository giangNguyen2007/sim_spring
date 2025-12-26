package demo;

public class UserService {
    private final UserRepository repo;

    // constructor injection (our container will pick this)
    public UserService(UserRepository repo) {
        this.repo = repo;
    }

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
