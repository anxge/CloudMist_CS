package WLYD.cloudMist_CS.storage;

public enum StorageType {
    YAML,
    MYSQL;
    
    public static StorageType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return YAML;
        }
    }
} 