package com.gestionmorgue.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class I18nUtilTest {

    @Test
    void testFrenchDefault() {
        I18nUtil.getInstance().setLanguage("fr");
        assertEquals("Connexion", I18nUtil.t("login.title"));
    }

    @Test
    void testEnglish() {
        I18nUtil.getInstance().setLanguage("en");
        assertEquals("Dashboard", I18nUtil.t("dashboard.title"));
    }

    @Test
    void testMissingKey() {
        I18nUtil.getInstance().setLanguage("fr");
        assertTrue(I18nUtil.t("nonexistent_key").startsWith("!"));
    }

    @Test
    void testSwitchLanguage() {
        I18nUtil.getInstance().setLanguage("fr");
        assertEquals("Se connecter", I18nUtil.t("login.button"));
        I18nUtil.getInstance().setLanguage("en");
        assertEquals("Sign in", I18nUtil.t("login.button"));
    }
}
