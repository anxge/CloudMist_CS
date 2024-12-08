package WLYD.cloudMist_CS.weapon;

public enum WeaponType {
    GLOCK17("modern_kinetic_gun{GunId:\"tacz:glock_17\",GunFireMode:\"SEMI\"}", "格洛克17", "IRON_SWORD"),
    AK47("modern_kinetic_gun{GunId:\"tacz:ak47\",GunFireMode:\"SEMI\"}", "AK-47", "IRON_SWORD"),
    M4A1("modern_kinetic_gun{GunId:\"tacz:m4a1\",GunFireMode:\"SEMI\"}", "M4A1", "IRON_SWORD"),
    DEAGLE("modern_kinetic_gun{GunId:\"tacz:deagle\",GunFireMode:\"SEMI\"}", "沙漠之鹰", "IRON_SWORD"),
    AWP("modern_kinetic_gun{GunId:\"tacz:ai_awp\",GunFireMode:\"SEMI\"}", "AWP狙击枪", "IRON_SWORD");

    private final String id;
    private final String displayName;
    private final String defaultMaterial;

    WeaponType(String id, String displayName, String defaultMaterial) {
        this.id = id;
        this.displayName = displayName;
        this.defaultMaterial = defaultMaterial;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultMaterial() {
        return defaultMaterial;
    }

    public static WeaponType getById(String id) {
        for (WeaponType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}