package core.services;

import core.dao.UserDAO;
import core.models.User;

public class AuthenticationService {
    private final UserDAO userDAO;

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean registerUser(String username, String role, String password) {
        return userDAO.registerUser(username, role, password);
    }

    public User login(String username, String password) {
        return userDAO.authenticateUser(username, password);
    }
}
