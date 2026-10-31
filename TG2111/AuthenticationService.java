package com.fot.ams.service;

import com.fot.ams.dao.UserDAO;
import com.fot.ams.exception.AuthenticationException;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.User;
import com.fot.ams.util.PasswordUtil;
import com.fot.ams.util.SessionManager;
import com.fot.ams.util.ValidationUtil;

/**
 * Service managing user authentication, sessions, and credential management.
 */
public class AuthenticationService {
    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user by username and password.
     * Sets the user in SessionManager upon success.
     *
     * @param username user login handle
     * @param password plain text password
     * @return authenticated User instance
     * @throws AuthenticationException on failure
     * @throws ValidationException on empty input
     */
    public User login(String username, String password) throws AuthenticationException, ValidationException {
        ValidationUtil.validateNotEmpty(username, "Username");
        ValidationUtil.validateNotEmpty(password, "Password");

        User user = userDAO.getUserByUsername(username.trim());
        if (user == null) {
            throw new AuthenticationException("Invalid username or password.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AuthenticationException("Your account is inactive. Please contact the administrator.");
        }

        boolean valid = PasswordUtil.verifyPassword(password, user.getPasswordHash());
        if (!valid) {
            throw new AuthenticationException("Invalid username or password.");
        }

        SessionManager.setCurrentUser(user);
        return user;
    }

    /**
     * Logs out current user.
     */
    public void logout() {
        SessionManager.logout();
    }

    /**
     * Changes password for a user.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword)
            throws AuthenticationException, ValidationException {
        ValidationUtil.validateNotEmpty(oldPassword, "Current password");
        ValidationUtil.validateNotEmpty(newPassword, "New password");

        if (newPassword.length() < 4) {
            throw new ValidationException("New password must be at least 4 characters long.");
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new AuthenticationException("User not found.");
        }

        if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password does not match.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }
}
