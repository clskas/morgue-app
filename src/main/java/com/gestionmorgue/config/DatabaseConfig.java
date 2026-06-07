package com.gestionmorgue.config;

import java.util.HashMap;
import java.util.Map;

public class DatabaseConfig {
    private static final String DB_DIR = System.getProperty("user.home") + "/.gestionmorgue/db";
    private static String activeProfile = "h2";
    private static String dbUrlOverride;

    public static void setProfile(String profile) {
        activeProfile = profile;
    }

    public static String getActiveProfile() {
        return activeProfile;
    }

    public static void setDbUrl(String url) {
        dbUrlOverride = url;
    }

    public static Map<String, String> getHibernateProperties() {
        if ("postgresql".equals(activeProfile)) {
            return getPostgresqlProperties();
        }
        return getH2Properties();
    }

    private static Map<String, String> getH2Properties() {
        Map<String, String> props = new HashMap<>();
        props.put("hibernate.connection.driver_class", "org.h2.Driver");
        props.put("hibernate.connection.url", dbUrlOverride != null ? dbUrlOverride : "jdbc:h2:" + DB_DIR + "/" + Constants.DB_NAME);
        props.put("hibernate.connection.username", Constants.DB_USER);
        props.put("hibernate.connection.password", Constants.DB_PASSWORD);
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "true");
        return props;
    }

    private static Map<String, String> getPostgresqlProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        String pgHost = System.getenv("PG_HOST") != null ? System.getenv("PG_HOST") : "localhost";
        String pgPort = System.getenv("PG_PORT") != null ? System.getenv("PG_PORT") : "5432";
        String pgDb = System.getenv("PG_DB") != null ? System.getenv("PG_DB") : "gestionmorgue";
        String pgUser = System.getenv("PG_USER") != null ? System.getenv("PG_USER") : "gestionmorgue";
        String pgPass = System.getenv("PG_PASSWORD") != null ? System.getenv("PG_PASSWORD") : "gestionmorgue";
        props.put("hibernate.connection.url", "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDb);
        props.put("hibernate.connection.username", pgUser);
        props.put("hibernate.connection.password", pgPass);
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.connection.pool_size", "10");
        return props;
    }
}
