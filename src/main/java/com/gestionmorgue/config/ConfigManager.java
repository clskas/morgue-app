package com.gestionmorgue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final String CONFIG_FILE = System.getProperty("user.home")
            + "/.gestionmorgue/config.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, Object> config;

    public ConfigManager() {
        config = load();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try {
                return mapper.readValue(file, Map.class);
            } catch (IOException e) {
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    public void save() {
        File file = new File(CONFIG_FILE);
        file.getParentFile().mkdirs();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde config: " + e.getMessage());
        }
    }

    public String getString(String key, String defaultValue) {
        return config.containsKey(key) ? config.get(key).toString() : defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return config.containsKey(key) ? Boolean.parseBoolean(config.get(key).toString()) : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        if (config.containsKey(key)) {
            Object val = config.get(key);
            if (val instanceof Number) return ((Number) val).intValue();
            try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) { return defaultValue; }
        }
        return defaultValue;
    }

    public void set(String key, Object value) {
        config.put(key, value);
        save();
    }
}
