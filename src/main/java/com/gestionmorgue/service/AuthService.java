package com.gestionmorgue.service;

import com.gestionmorgue.dao.UserDao;
import com.gestionmorgue.model.AuditLog;
import com.gestionmorgue.model.User;
import com.gestionmorgue.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public User authenticate(String username, String password) {
        Optional<User> optUser = userDao.findByUsername(username);
        if (optUser.isEmpty()) {
            throw new RuntimeException("Utilisateur introuvable");
        }
        User user = optUser.get();
        if (!user.isActive()) {
            throw new RuntimeException("Compte désactivé");
        }
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new RuntimeException("Mot de passe incorrect");
        }
        user.setLastLogin(LocalDateTime.now());
        userDao.update(user);
        SessionManager.getInstance().login(user);
        logAction(user, "LOGIN", "Connexion utilisateur");
        return user;
    }

    public void logout() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            logAction(user, "LOGOUT", "Déconnexion utilisateur");
        }
        SessionManager.getInstance().logout();
    }

    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }
        user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userDao.update(user);
        logAction(user, "CHANGE_PASSWORD", "Changement de mot de passe");
    }

    public User createUser(String username, String password, String fullName, String role, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setFullName(fullName);
        user.setRole(role);
        user.setEmail(email);
        user.setActive(true);
        return userDao.save(user);
    }

    private void logAction(User user, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        // persist audit log
        try (var session = com.gestionmorgue.util.DatabaseManager.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            session.persist(log);
            tx.commit();
        } catch (Exception ignored) {}
    }
}
