package WLYD.cloudMist_CS.commands;

import WLYD.cloudMist_CS.CloudMist_CS;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class PlayerCommand implements CommandExecutor, TabCompleter {
    private final CloudMist_CS plugin;
    
    public PlayerCommand(CloudMist_CS plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join":
                if (!player.hasPermission("cloudmist.player.join")) {
                    player.sendMessage("§c你没有权限使用此命令！");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§c用法: /cmcs join <游戏名>");
                    return true;
                }
                handleJoin(player, args[1]);
                break;
                
            case "leave":
                if (!player.hasPermission("cloudmist.player.leave")) {
                    player.sendMessage("§c你没有权限使用此命令！");
                    return true;
                }
                handleLeave(player);
                break;
                
            default:
                showHelp(player);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("join");
            completions.add("leave");
        }
        return completions;
    }

    private void showHelp(Player player) {
        player.sendMessage("§6=== CloudMist CS 玩家指令 ===");
        player.sendMessage("§f/cmcs join <游戏名> §7- 加入游戏");
        player.sendMessage("§f/cmcs leave §7- 离开游戏");
        player.sendMessage("§7提示: 使用 TAB 键可以自动补全命令");
    }
    
    private void handleJoin(Player player, String gameName) {
        plugin.getGameManager().joinGame(player, gameName);
    }
    
    private void handleLeave(Player player) {
        plugin.getGameManager().leaveGame(player);
    }
} 