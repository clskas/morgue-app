package com.gestionmorgue.rest;

import com.gestionmorgue.util.DatabaseManager;
import org.hibernate.Session;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

public class TokenManager {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final long TOKEN_TTL_SECONDS = 86400;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static String serverSecret;

    public static synchronized String getServerSecret() {
        if (serverSecret == null) {
            serverSecret = loadOrGenerateSecret();
        }
        return serverSecret;
    }

    public static String generateToken(String username, String role) {
        try {
            long issued = Instant.now().getEpochSecond();
            long expires = issued + TOKEN_TTL_SECONDS;
            String payload = username + ":" + role + ":" + issued + ":" + expires;
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(getServerSecret().getBytes(), HMAC_ALGO));
            String signature = HexFormat.of().formatHex(mac.doFinal(payload.getBytes()));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    (payload + ":" + signature).getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération token", e);
        }
    }

    public static TokenValidation validateToken(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length < 5) return TokenValidation.invalid("Format invalide");

            String username = parts[0];
            String role = parts[1];
            long expires = Long.parseLong(parts[3]);
            String signature = parts[4];

            String payload = parts[0] + ":" + parts[1] + ":" + parts[2] + ":" + parts[3];
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(getServerSecret().getBytes(), HMAC_ALGO));
            String expectedSig = HexFormat.of().formatHex(mac.doFinal(payload.getBytes()));

            if (!signature.equals(expectedSig)) return TokenValidation.invalid("Signature invalide");
            if (Instant.now().getEpochSecond() > expires) return TokenValidation.invalid("Token expiré");

            return TokenValidation.valid(username, role);
        } catch (Exception e) {
            return TokenValidation.invalid(e.getMessage());
        }
    }

    private static String loadOrGenerateSecret() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<String> q = cb.createQuery(String.class);
            Root<com.gestionmorgue.model.AuditLog> root = q.from(com.gestionmorgue.model.AuditLog.class);
            q.select(root.get("details")).where(
                    cb.equal(root.get("action"), "API_SECRET"));
            var results = session.createQuery(q).setMaxResults(1).list();
            if (!results.isEmpty() && results.get(0) != null) {
                return results.get(0);
            }
        } catch (Exception ignored) {}
        byte[] key = new byte[32];
        RANDOM.nextBytes(key);
        String secret = HexFormat.of().formatHex(key);
        return secret;
    }

    public static class TokenValidation {
        private final boolean valid;
        private final String username;
        private final String role;
        private final String error;

        private TokenValidation(boolean valid, String username, String role, String error) {
            this.valid = valid;
            this.username = username;
            this.role = role;
            this.error = error;
        }

        public static TokenValidation valid(String username, String role) {
            return new TokenValidation(true, username, role, null);
        }
        public static TokenValidation invalid(String error) {
            return new TokenValidation(false, null, null, error);
        }
        public boolean isValid() { return valid; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getError() { return error; }
    }
}
