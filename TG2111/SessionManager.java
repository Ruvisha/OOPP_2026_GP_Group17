package com.fot.ams.util;

import com.fot.ams.model.User;

/**
 * Manages user session state upon successful authentication.
 */
public class SessionManager {
    private static User currentUser;
    private static long loginTimestamp;

    private SessionManager() {
    }

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
        loginTimestamp = System.currentTimeMillis();
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized void logout() {
        currentUser = null;
        loginTimestamp = 0;
    }

    public static synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRoleId() == 1;
    }

    public static synchronized boolean isLecturer() {
        return currentUser != null && currentUser.getRoleId() == 2;
    }

    public static synchronized boolean isTechnicalOfficer() {
        return currentUser != null && currentUser.getRoleId() == 3;
    }

    public static synchronized boolean isUndergraduate() {
        return currentUser != null && currentUser.getRoleId() == 4;
    }

    public static synchronized long getLoginTimestamp() {
        return loginTimestamp;
    }
}
