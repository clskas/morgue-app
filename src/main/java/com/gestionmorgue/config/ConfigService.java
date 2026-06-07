package com.gestionmorgue.config;

public class ConfigService {
    private static final ConfigService instance = new ConfigService();
    private final ConfigManager config;

    private ConfigService() {
        config = new ConfigManager();
    }

    public static ConfigService getInstance() { return instance; }
    public ConfigManager getConfig() { return config; }

    // Thème
    public String getTheme() { return config.getString("theme", "clair"); }
    public void setTheme(String theme) { config.set("theme", theme); }
    public String getCustomTheme() { return config.getString("custom_theme", ""); }
    public void setCustomTheme(String css) { config.set("custom_theme", css); }

    // Langue
    public String getLanguage() { return config.getString("language", "fr"); }
    public void setLanguage(String lang) { config.set("language", lang); }

    // Mise à jour
    public boolean isUpdateCheckEnabled() { return config.getBoolean("update_check_enabled", true); }
    public void setUpdateCheckEnabled(boolean enabled) { config.set("update_check_enabled", enabled); }
    public String getIgnoredVersion() { return config.getString("ignored_version", ""); }
    public void setIgnoredVersion(String version) { config.set("ignored_version", version); }

    // Personnalisation hôpital
    public String getHospitalName() { return config.getString("hospital_name", ""); }
    public void setHospitalName(String name) { config.set("hospital_name", name); }

    public String getHospitalAddress() { return config.getString("hospital_address", ""); }
    public void setHospitalAddress(String addr) { config.set("hospital_address", addr); }

    public String getHospitalPhone() { return config.getString("hospital_phone", ""); }
    public void setHospitalPhone(String phone) { config.set("hospital_phone", phone); }

    public String getHospitalLogoPath() { return config.getString("hospital_logo", ""); }
    public void setHospitalLogoPath(String path) { config.set("hospital_logo", path); }

    // Format numéro de dossier
    public String getDossierPrefix() { return config.getString("dossier_prefix", "DOS"); }
    public void setDossierPrefix(String prefix) { config.set("dossier_prefix", prefix); }

    // Police / taille
    public int getFontSize() { return config.getInt("font_size", 13); }
    public void setFontSize(int size) { config.set("font_size", size); }
}
