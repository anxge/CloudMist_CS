package WLYD.cloudMist_CS.weapon;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeaponLoadoutManager {
    private final Map<UUID, Map<String, WeaponType>> playerLoadouts = new HashMap<>();
    
    public void saveLoadout(Player player, String name, WeaponType weapon) {
        playerLoadouts.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                     .put(name, weapon);
    }
    
    public WeaponType getLoadout(Player player, String name) {
        return playerLoadouts.getOrDefault(player.getUniqueId(), new HashMap<>())
                           .get(name);
    }
} 