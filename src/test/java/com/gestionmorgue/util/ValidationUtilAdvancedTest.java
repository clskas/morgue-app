package com.gestionmorgue.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilAdvancedTest {

    @Test
    void testValidNir() {
        assertTrue(ValidationUtil.isValidNir("1 85 05 45 123 456 75"));
        assertTrue(ValidationUtil.isValidNir("185054512345675"));
        assertTrue(ValidationUtil.isValidNir(""));  // empty = valid (optional field)
        assertTrue(ValidationUtil.isValidNir(null)); // null = valid
    }

    @Test
    void testInvalidNir() {
        assertFalse(ValidationUtil.isValidNir("123"));
        assertFalse(ValidationUtil.isValidNir("ABCDEFGHIJKLM"));
    }

    @Test
    void testValidPhone() {
        assertTrue(ValidationUtil.isValidPhone("0612345678"));
        assertTrue(ValidationUtil.isValidPhone("+33612345678"));
        assertTrue(ValidationUtil.isValidPhone(""));
        assertTrue(ValidationUtil.isValidPhone(null));
    }

    @Test
    void testInvalidPhone() {
        assertFalse(ValidationUtil.isValidPhone("123"));
        assertFalse(ValidationUtil.isValidPhone("061234567"));
    }

    @Test
    void testValidEmail() {
        assertTrue(ValidationUtil.isValidEmail("test@test.com"));
        assertTrue(ValidationUtil.isValidEmail(""));
        assertTrue(ValidationUtil.isValidEmail(null));
    }

    @Test
    void testInvalidEmail() {
        assertFalse(ValidationUtil.isValidEmail("test"));
        assertFalse(ValidationUtil.isValidEmail("test@"));
    }
}
