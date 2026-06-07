package com.gestionmorgue.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18nUtil {
    private static I18nUtil instance;
    private ResourceBundle bundle;
    private Locale currentLocale;

    private I18nUtil() {
        setLanguage("fr");
    }

    public static I18nUtil getInstance() {
        if (instance == null) instance = new I18nUtil();
        return instance;
    }

    public void setLanguage(String lang) {
        currentLocale = Locale.of(lang);
        bundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
    }

    public String get(String key, Object... args) {
        if (!bundle.containsKey(key)) return "!" + key + "!";
        String pattern = bundle.getString(key);
        return args.length > 0 ? MessageFormat.format(pattern, args) : pattern;
    }

    public Locale getCurrentLocale() { return currentLocale; }
    public String getCurrentLanguage() { return currentLocale.getLanguage(); }

    public static String t(String key, Object... args) {
        return getInstance().get(key, args);
    }
}
