package com.gestionmorgue.config;

import com.gestionmorgue.config.ConfigManager;

public class ConfigService {
    private static final ConfigService instance = new ConfigService();
    private final ConfigManager config;

    private ConfigService() {
        config = new ConfigManager();
    }

    public static ConfigService getInstance() { return instance; }

    public ConfigManager getConfig() { return config; }

    public String getTheme() { return config.getString("theme", "clair"); }
    public void setTheme(String theme) { config.set("theme", theme); }

    public String getLanguage() { return config.getString("language", "fr"); }
    public void setLanguage(String lang) { config.set("language", lang); }

    public boolean isUpdateCheckEnabled() { return config.getBoolean("update_check_enabled", true); }
    public void setUpdateCheckEnabled(boolean enabled) { config.set("update_check_enabled", enabled); }

    public String getIgnoredVersion() { return config.getString("ignored_version", ""); }
    public void setIgnoredVersion(String version) { config.set("ignored_version", version); }

    public String getCustomTheme() { return config.getString("custom_theme", ""); }
    public void setCustomTheme(String css) { config.set("custom_theme", css); }
}
