package core.dao;

import core.models.User;
import core.userfactory.UserFactory;
import core.utils.EncryptionUtil;

import java.sql.*;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }
    public boolean registerUser(String username, String role, String password) {
        String sql = "INSERT INTO tbl_users (username, password_hash, user_role) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String hashedPassword = EncryptionUtil.hashPassword(password);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM tbl_users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String inputHash = EncryptionUtil.hashPassword(password);

                System.out.println("[DEBUG] Username found: " + username);
                System.out.println("[DEBUG] Stored Hash: " + storedHash);
                System.out.println("[DEBUG] Input Hash: " + inputHash);

                if (storedHash.equals(inputHash)) {
                    System.out.println("[DEBUG] Password match. Logging in...");
                    int id = rs.getInt("id");
                    String role = rs.getString("user_role");
                    // Use factory here
                    return UserFactory.createUser(id, username, role);
                } else {
                    System.out.println("[DEBUG] Password mismatch.");
                }
            } else {
                System.out.println("[DEBUG] Username not found.");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Login error: " + e.getMessage());
        }
        return null;
    }

}
