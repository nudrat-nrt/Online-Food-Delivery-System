package Login;

public class LoginProxy implements LoginService {

    private RealLoginService realService = new RealLoginService();

    @Override
    public boolean login(String username, String password) {
        System.out.println("Checking access...");
        return realService.login(username, password);
    }
}

