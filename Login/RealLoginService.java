package Login;

public class RealLoginService implements LoginService {

    @Override
    public boolean login(String username, String password) {
        return username.equals("admin") && password.equals("1234");
    }
}

