package com.gestionmorgue.config;

public final class Constants {
    public static final String APP_NAME = "Gestion Morgue";
    public static final String APP_VERSION = "1.0.0";
    public static final String UPDATE_URL = System.getProperty("update.url",
            "https://github.com/clskas/morgue-app/releases/latest/download/version.json");
    public static final String DB_NAME = "gestionmorgue";
    public static final String DB_USER = "sa";
    public static final String DB_PASSWORD = "";

    public static final String[] ROLES = {"ADMIN", "MEDECIN", "THANATOPRACTEUR", "GREFFIER"};

    private Constants() {}
}
