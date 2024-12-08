package WLYD.cloudMist_CS.weapon;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.game.Team;
import org.bukkit.configuration.ConfigurationSection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;

public class WeaponManager {
    private static final Logger LOGGER = Logger.getLogger(WeaponManager.class.getName());
    private static final String TACZ_MOD_ID = "tacz";
    private static final String FORGE_CLASS = "net.minecraftforge.fml.ModList";
    
    private final CloudMist_CS plugin;
    private final WeaponLoadoutManager loadoutManager;
    private boolean taczEnabled;
    private final ConcurrentHashMap<String, String> weaponMapping;
    
    public WeaponManager(CloudMist_CS plugin, boolean taczEnabled) {
        this.plugin = plugin;
        this.loadoutManager = new WeaponLoadoutManager();
        this.weaponMapping = new ConcurrentHashMap<>();
        this.taczEnabled = taczEnabled;
        
        loadWeaponMapping();
        initializeTaczSupport();
    }
    
    private void loadWeaponMapping() {
        ConfigurationSection mappingSection = plugin.getConfig()
            .getConfigurationSection("mods.tacz.weapons.mapping");
        if (mappingSection != null) {
            for (String key : mappingSection.getKeys(false)) {
                weaponMapping.put(key, mappingSection.getString(key));
            }
        }
    }
    
    private void initializeTaczSupport() {
        if (plugin.getConfig().getBoolean("mods.tacz.enabled", true)) {
            if (checkTaczMod()) {
                taczEnabled = true;
                LOGGER.info("§a成功加载 TACZ 模组，启用TACZ武器系统!");
            } else {
                taczEnabled = false;
                LOGGER.warning("§c未找到TACZ模组，将使用原版武器系统!");
            }
        }
    }
    
    public void giveDefaultLoadout(Player player, Team team) {
        String teamKey = team == Team.TERRORIST ? "terrorist" : "counter_terrorist";
        String primaryWeapon = plugin.getConfigManager().getWeaponsConfig()
            .getString("loadouts." + teamKey + ".primary", "GLOCK17");
        boolean hasArmor = plugin.getConfigManager().getWeaponsConfig()
            .getBoolean("loadouts." + teamKey + ".armor", false);
        boolean hasKnife = plugin.getConfigManager().getWeaponsConfig()
            .getBoolean("loadouts." + teamKey + ".knife", true);

        player.getInventory().clear();
        
        giveWeapon(player, primaryWeapon);
        if (hasKnife) {
            giveWeapon(player, "KNIFE");
        }
        if (hasArmor) {
            player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        }
    }
    
    public void giveDefaultWeapons(Player player, Team team) {
        // 只给予一把默认武器
        if (team == Team.TERRORIST) {
            giveWeapon(player, WeaponType.GLOCK17.getId());
        } else if (team == Team.COUNTER_TERRORIST) {
            giveWeapon(player, WeaponType.GLOCK17.getId());
        }
    }
    
    public void giveWeapon(Player player, String weaponId) {
        if (weaponId == null || weaponId.isEmpty()) {
            plugin.getLogger().warning("尝试给予空武器ID给玩家: " + player.getName());
            return;
        }
        
        if (taczEnabled) {
            try {
                String command = String.format("give %s %s", 
                    player.getName(),
                    weaponId
                );
                
                plugin.getLogger().fine("执行武器给予命令: " + command);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                
                plugin.getLogger().fine("已给予玩家 " + player.getName() + " 武器: " + weaponId);
            } catch (Exception e) {
                player.sendMessage("§c获取武器失败，请联系管理员");
                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().warning("给予武器失败: " + weaponId);
                    e.printStackTrace();
                }
            }
        } else {
            // 如果TACZ未启用，给予普通物品
            ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = weapon.getItemMeta();
            meta.setDisplayName("§f" + weaponId);
            List<String> lore = new ArrayList<>();
            lore.add("§7模拟武器 - TACZ模组未启用");
            meta.setLore(lore);
            weapon.setItemMeta(meta);
            player.getInventory().setItemInMainHand(weapon);
            player.sendMessage("§e由于TACZ模组未启用，使用模拟武器替代");
        }
    }
    
    private WeaponType getCurrentWeapon(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() != Material.AIR) {
            return WeaponType.getById(item.getType().name().toLowerCase());
        }
        return null;
    }
    
    public void saveLoadout(Player player) {
        WeaponType currentWeapon = getCurrentWeapon(player);
        if (currentWeapon != null) {
            loadoutManager.saveLoadout(player, "default", currentWeapon);
        }
    }
    
    public void loadLoadout(Player player) {
        WeaponType weapon = loadoutManager.getLoadout(player, "default");
        if (weapon != null) {
            giveWeapon(player, weapon.getId());
        }
    }
    
    public void dropWeapon(Player player) {}
    
    public void reloadConfig() {
        // 清除现有的映射
        weaponMapping.clear();
        taczEnabled = false;
        
        // 重新加载配置
        loadWeaponMapping();
        initializeTaczSupport();
        
        if (plugin.getConfig().getBoolean("debug")) {
            LOGGER.info("武器管理器配置已重新加载");
            LOGGER.info("TACZ模组支持: " + (taczEnabled ? "已启用" : "未启用"));
            LOGGER.info("已加载武器映射: " + weaponMapping.toString());
        }
    }
    
    public void giveWeapon(Player player, WeaponType weaponType) {
        if (taczEnabled) {
            // TACZ武器系统
            try {
                String weaponFormat = plugin.getConfigManager().getModsConfig()
                    .getString("tacz.weapons.mapping." + weaponType.name(), weaponType.getId());
                String command = "give " + player.getName() + " " + weaponFormat;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                player.sendMessage("§a你获得了一把 " + weaponType.getDisplayName());
            } catch (Exception e) {
                handleFallbackWeapon(player, weaponType);
            }
        } else {
            handleFallbackWeapon(player, weaponType);
        }
    }
    
    private void handleFallbackWeapon(Player player, WeaponType weaponType) {
        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = weapon.getItemMeta();
        meta.setDisplayName("§f" + weaponType.getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add("§7模拟武器");
        meta.setLore(lore);
        weapon.setItemMeta(meta);
        player.getInventory().addItem(weapon);
        player.sendMessage("§e使用模拟武器替代: " + weaponType.getDisplayName());
    }
    
    private boolean isForgeEnvironment() {
        try {
            Class.forName(FORGE_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private boolean checkTaczMod() {
        try {
            // 检查Forge环境
            if (!isForgeEnvironment()) {
                if (plugin.getConfig().getBoolean("debug")) {
                    LOGGER.info("§c未检测到Forge环境");
                }
                return false;
            }

            // 检查模组是否加载
            Class<?> forgeClass = Class.forName(FORGE_CLASS);
            Object modList = forgeClass.getMethod("get").invoke(null);
            boolean isLoaded = (boolean) forgeClass.getMethod("isLoaded", String.class)
                .invoke(modList, TACZ_MOD_ID);

            if (isLoaded) {
                if (plugin.getConfig().getBoolean("debug")) {
                    LOGGER.info("§a成功检测到TACZ模组");
                }
                return true;
            }

            if (plugin.getConfig().getBoolean("debug")) {
                LOGGER.info("§c未能在Forge中找到TACZ模组");
            }
            return false;
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
            return false;
        }
    }
    
    public boolean hasWeapon(Player player, String weaponId) {
        if (taczEnabled) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.hasItemMeta()) {
                    String nbtString = item.getItemMeta().toString();
                    if (nbtString.contains(weaponId)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return player.getInventory().contains(Material.DIAMOND_SWORD);
        }
    }
    
    public boolean isTaczEnabled() {
        return taczEnabled;
    }
    
    public int getWeaponPrice(String weaponId) {
        return plugin.getConfigManager().getWeaponsConfig().getInt("prices." + weaponId, 0);
    }
    
    public double getFriendlyFireMultiplier() {
        return plugin.getConfigManager().getModsConfig().getDouble("tacz.settings.friendly_fire_multiplier", 0.33);
    }
} 