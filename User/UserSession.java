package User;

public class UserSession {

    private static UserSession instance;
    private String username;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null)
            instance = new UserSession();
        return instance;
    }

    public void login(String username) {
        this.username = username;
    }

    public String getUser() {
        return username;
    }
}

