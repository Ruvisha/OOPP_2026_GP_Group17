package com.fot.ams.service;

import com.fot.ams.dao.UserDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Undergraduate;
import com.fot.ams.model.User;
import com.fot.ams.util.PasswordUtil;
import com.fot.ams.util.ValidationUtil;

import java.util.List;

/**
 * Service handling business rules and operations for Users.
 */
public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public List<User> getUsersByRole(int roleId) {
        return userDAO.getUsersByRole(roleId);
    }

    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    public boolean createUser(User user, String plainPassword) throws ValidationException {
        ValidationUtil.validateNotEmpty(user.getUsername(), "Username");
        ValidationUtil.validateNotEmpty(user.getFullName(), "Full name");
        ValidationUtil.validateEmail(user.getEmail());
        ValidationUtil.validatePhone(user.getPhone());
        ValidationUtil.validateNotEmpty(plainPassword, "Password");

        if (userDAO.isUsernameTaken(user.getUsername().trim(), 0)) {
            throw new ValidationException("Username '" + user.getUsername() + "' is already registered.");
        }

        if (userDAO.isEmailTaken(user.getEmail().trim(), 0)) {
            throw new ValidationException("Email '" + user.getEmail() + "' is already in use.");
        }

        if (user instanceof Undergraduate ug) {
            ValidationUtil.validateNotEmpty(ug.getStudentRegNo(), "Student Registration Number");
            if (userDAO.isRegNoTaken(ug.getStudentRegNo().trim(), 0)) {
                throw new ValidationException("Registration No '" + ug.getStudentRegNo() + "' is already registered.");
            }
        }

        user.setPasswordHash(PasswordUtil.hashPassword(plainPassword.trim()));
        return userDAO.addUser(user);
    }

    public boolean updateUser(User user) throws ValidationException {
        ValidationUtil.validateNotEmpty(user.getFullName(), "Full name");
        ValidationUtil.validateEmail(user.getEmail());
        ValidationUtil.validatePhone(user.getPhone());

        if (userDAO.isEmailTaken(user.getEmail().trim(), user.getUserId())) {
            throw new ValidationException("Email '" + user.getEmail() + "' is already in use.");
        }

        if (user instanceof Undergraduate ug && ug.getStudentRegNo() != null) {
            if (userDAO.isRegNoTaken(ug.getStudentRegNo().trim(), user.getUserId())) {
                throw new ValidationException("Registration No '" + ug.getStudentRegNo() + "' is already registered.");
            }
        }

        return userDAO.updateUser(user);
    }

    public boolean updateContactDetails(int userId, String email, String phone, String address) throws ValidationException {
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePhone(phone);
        return userDAO.updateContactDetails(userId, email.trim(), phone != null ? phone.trim() : "", address != null ? address.trim() : "");
    }

    public boolean updateProfilePicture(int userId, String picturePath) {
        return userDAO.updateProfilePicture(userId, picturePath);
    }

    public boolean deleteUser(int userId) {
        return userDAO.deleteUser(userId);
    }
}
