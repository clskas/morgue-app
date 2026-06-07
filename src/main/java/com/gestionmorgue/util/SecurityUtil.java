package com.gestionmorgue.util;

import com.gestionmorgue.model.User;

public class SecurityUtil {

    public static void requireAdmin() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null || !user.isAdmin()) {
            throw new SecurityException("Accès réservé aux administrateurs");
        }
    }

    public static void requireRole(String... roles) {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            throw new SecurityException("Utilisateur non connecté");
        }
        for (String role : roles) {
            if (user.getRole().equals(role)) return;
        }
        throw new SecurityException("Accès réservé aux rôles: " + String.join(", ", roles));
    }

    public static boolean isAdmin() {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null && user.isAdmin();
    }

    public static boolean hasRole(String role) {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null && user.getRole().equals(role);
    }
}
