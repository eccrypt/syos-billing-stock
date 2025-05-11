package core.dao;

import core.utils.PasswordHashUtil;


import java.sql.*;


public class UserDAO {
    private Connection connection;

    // This is a constructor that will be called whenever the UserDAO
    //    Class is called anywhere in the application
    public UserDAO(Connection connection){
        this.connection = connection;
    }
//    User Registration Function
    public void registerUser(String username, String password,String userRole) throws SQLException{
        String hashed = PasswordHashUtil.hashPassword(password);
        String sql = "INSERT INTO tbl_users (username,password_hash,user_role) VALUES(?,?,?) ";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, username);
            stmt.setString(2, hashed);
            stmt.setString(3, userRole);
            stmt.executeUpdate();
        }
    }
}
