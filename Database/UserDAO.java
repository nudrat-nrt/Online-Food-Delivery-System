package Database;

import Login.User;
import java.sql.*;

public class UserDAO {
    private Connection connection;
    
    public UserDAO() {
        connection = SQLiteConnection.getInstance().getConnection();
    }
    
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ? AND is_active = 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role")
                );
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ Authentication error: " + e.getMessage());
        }
        return null;
    }
    
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error checking username: " + e.getMessage());
            return false;
        }
    }
    
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error checking email: " + e.getMessage());
            return false;
        }
    }
    
    public boolean registerUser(User user, String email, String fullName, String phone) {
        String sql = "INSERT INTO users (username, password_hash, email, full_name, phone, role) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.setString(5, phone);
            pstmt.setString(6, user.getRole());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Registration error: " + e.getMessage());
            return false;
        }
    }
    
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role")
                );
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user: " + e.getMessage());
        }
        return null;
    }
}