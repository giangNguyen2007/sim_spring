package demo;

public class UserRepository {
    public String findUserName(long id) {
        return "user-" + id;
    }
}