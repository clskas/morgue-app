package com.gestionmorgue.util;

import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ValidationUtil {

    private static final String ERROR_STYLE = "-fx-border-color: #c62828; -fx-border-width: 1.5;";

    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        public boolean isValid() { return errors.isEmpty(); }
        public List<String> getErrors() { return errors; }
        public String getErrorMessage() { return String.join("\n", errors); }
        public void addError(String error) { errors.add(error); }
    }

    public static ValidationResult validateRequired(TextInputControl field, String fieldName) {
        ValidationResult result = new ValidationResult();
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            field.setStyle(ERROR_STYLE);
            result.addError(fieldName + " est obligatoire");
        } else {
            field.setStyle(null);
        }
        return result;
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static void clearErrorStyle(Control control) {
        control.setStyle(null);
    }

    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("<[^>]*>", "");
    }

    public static boolean isValidNir(String nir) {
        if (nir == null || nir.isEmpty()) return true;
        String cleaned = nir.replaceAll("[^0-9]", "");
        if (cleaned.length() != 13 && cleaned.length() != 15) return false;
        if (cleaned.length() == 15) cleaned = cleaned.substring(0, 13);
        String gender = cleaned.substring(0, 1);
        if (!"123".contains(gender)) return false;
        String year = cleaned.substring(1, 3);
        String month = cleaned.substring(3, 5);
        int m = Integer.parseInt(month);
        if (m < 1 || m > 12) return false;
        String dept = cleaned.substring(5, 7);
        if ("00".equals(dept) || "20".equals(dept)) return false;
        if (cleaned.length() == 15) {
            long num = Long.parseLong(cleaned.substring(0, 13));
            long key = Long.parseLong(cleaned.substring(13));
            return (97 - (num % 97)) == key;
        }
        return true;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) return true;
        return phone.replaceAll("[^0-9+]", "").matches("^(\\+33|0)[1-9]\\d{8}$");
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return true;
        return email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");
    }
}
