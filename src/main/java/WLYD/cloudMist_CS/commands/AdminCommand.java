package WLYD.cloudMist_CS.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.Team;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;

public class AdminCommand implements CommandExecutor, TabCompleter {
    private final CloudMist_CS plugin;
    private final Map<UUID, String> confirmations = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 3000; // 3秒冷却时间
    
    public AdminCommand(CloudMist_CS plugin) {
        this.plugin = plugin;
    }
    
    // 检查命令冷却
    private boolean checkCooldown(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = cooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage("§c请等待 " + (timeLeft/1000) + " 秒后再使用此命令");
                return false;
            }
        }
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + COOLDOWN_TIME);
        return true;
    }
    
    // 处理命令确认
    private boolean handleConfirmation(Player player, String gameName, String action) {
        if (confirmations.containsKey(player.getUniqueId())) {
            String confirmedGame = confirmations.get(player.getUniqueId());
            if (confirmedGame.equals(gameName)) {
                confirmations.remove(player.getUniqueId());
                return true;
            }
        }
        
        confirmations.put(player.getUniqueId(), gameName);
        player.sendMessage("§e确认" + action + "游戏 " + gameName + "？请在30秒内再次输入此命令确认。");
        
        // 30秒后自动移除确认
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            confirmations.remove(player.getUniqueId());
        }, 20L * 30); // 30秒
        
        return false;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查基础管理员权限
        if (!sender.hasPermission("cloudmist.admin")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        // 对特定命令检查具体权限
        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();
            String permission = "cloudmist.admin." + subCommand;
            if (!sender.hasPermission(permission)) {
                sender.sendMessage("§c你没有权限使用此命令！");
                return true;
            }
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /cmcsadmin create <游戏名称>");
                    return true;
                }
                handleCreate(sender, args[1]);
                break;
                
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /cmcsadmin delete <游戏名称>");
                    return true;
                }
                handleDelete(sender, args[1]);
                break;
                
            case "list":
                handleList(sender);
                break;
                
            case "info":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /cmcsadmin info <游戏名称>");
                    return true;
                }
                handleInfo(sender, args[1]);
                break;
                
            case "setspawn":
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /cmcsadmin setspawn <游戏名称> <t/ct>");
                    return true;
                }
                handleSetSpawn(sender, args[1], args[2]);
                break;
                
            case "setbomb":
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /cmcsadmin setbomb <游戏名称> <A/B> [半径]");
                    return true;
                }
                double radius = args.length > 3 ? Double.parseDouble(args[3]) : 3.0;
                handleSetBombSite(sender, args[1], args[2], radius);
                break;
                
            case "settings":
                handleSettings(sender, args);
                break;
                
            case "reload":
                handleReload(sender);
                break;
                
            case "start":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /cmcsadmin start <游戏名称>");
                    return true;
                }
                handleStart(sender, args[1]);
                break;
                
            case "stop":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /cmcsadmin stop <游戏名称>");
                    return true;
                }
                handleStop(sender, args[1]);
                break;
                
            case "setlobby":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /cmcsadmin setlobby <游戏名称>");
                    return true;
                }
                handleSetLobby(sender, args[1]);
                break;
                
            case "setbuyzone":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /cmcsadmin setbuyzone <游戏名称>");
                    return true;
                }
                handleSetBuyZone(sender, args[1]);
                break;
                
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /cmcsadmin reset <游戏名称>");
                    return true;
                }
                handleReset(sender, args[1]);
                break;
                
            case "debug":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /cmcsadmin debug <游戏名称>");
                    return true;
                }
                handleDebug(sender, args[1]);
                break;
                
            case "backup":
                handleBackup(sender);
                break;
                
            default:
                sendHelpMessage(sender);
                break;
        }
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("create");
            completions.add("delete");
            completions.add("list");
            completions.add("info");
            completions.add("setspawn");
            completions.add("setbomb");
            completions.add("settings");
            completions.add("reload");
            completions.add("start");
            completions.add("stop");
            completions.add("setlobby");
            completions.add("setbuyzone");
            completions.add("reset");
            completions.add("debug");
            completions.add("backup");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "setlobby":
                case "setspawn":
                case "setbomb":
                case "delete":
                case "info":
                case "start":
                case "stop":
                case "setbuyzone":
                case "reset":
                case "debug":
                    plugin.getGameManager().getGames().keySet().forEach(completions::add);
                    break;
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "setspawn":
                    completions.add("t");
                    completions.add("ct");
                    break;
                case "setbomb":
                    completions.add("A");
                    completions.add("B");
                    break;
            }
        }
        
        return completions;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6========= CloudMist CS 管理员指令 =========");
        sender.sendMessage("§f/cmcsadmin create <游戏名称> - 创建新游戏");
        sender.sendMessage("§f/cmcsadmin delete <游戏名称> - 删除游戏");
        sender.sendMessage("§f/cmcsadmin list - 查看游戏列表");
        sender.sendMessage("§f/cmcsadmin info <游戏名称> - 查看游戏信息");
        sender.sendMessage("§f/cmcsadmin setspawn <游戏名称> <t/ct> - 设置出生点");
        sender.sendMessage("§f/cmcsadmin setbomb <游戏名称> <A/B> [半径] - 设置炸弹点");
        sender.sendMessage("§f/cmcsadmin setlobby <游戏名称> - 设置大厅");
        sender.sendMessage("§f/cmcsadmin setbuyzone <游戏名称> - 设置购买区");
        sender.sendMessage("§f/cmcsadmin start <游戏名称> - 强制开始游戏");
        sender.sendMessage("§f/cmcsadmin stop <游戏名称> - 强制停止游戏");
        sender.sendMessage("§f/cmcsadmin reset <游戏名称> - 重置游戏");
        sender.sendMessage("§f/cmcsadmin settings <选项> <值> - 修改游戏设置");
        sender.sendMessage("§f/cmcsadmin reload - 重新加载配置");
        sender.sendMessage("§f/cmcsadmin debug <游戏名称> - 查看调试信息");
        sender.sendMessage("§f/cmcsadmin backup - 备份游戏数据");
        sender.sendMessage("§6===============================");
    }
    
    private void handleCreate(CommandSender sender, String gameName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行！");
            return;
        }
        
        Player player = (Player) sender;
        if (!checkCooldown(player)) {
            return;
        }

        Location location = player.getLocation();
        
        try {
            if (plugin.getGameManager().createGame(gameName, location)) {
                sender.sendMessage("§a成功创建游戏 " + gameName);
                plugin.getLogger().info("管理员 " + player.getName() + " 创建了新游戏 " + gameName);
            } else {
                sender.sendMessage("§c创建游戏失败，该名称可能已被使用");
            }
        } catch (Exception e) {
            sender.sendMessage("§c创建游戏时发生错误: " + e.getMessage());
            plugin.getLogger().warning("创建游戏 " + gameName + " 时发生错误: " + e.getMessage());
            if (plugin.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }
    
    private void handleDelete(CommandSender sender, String gameName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行！");
            return;
        }
        
        Player player = (Player) sender;
        if (!checkCooldown(player)) {
            return;
        }

        Map<String, CSGame> games = plugin.getGameManager().getGames();
        if (!games.containsKey(gameName)) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }

        if (!handleConfirmation(player, gameName, "删除")) {
            return;
        }

        // 执行删除操作
        CSGame game = games.remove(gameName);
        if (game != null) {
            game.stop(); // 确保游戏停止
            plugin.getLogger().info("管理员 " + player.getName() + " 删除了游戏 " + gameName);
            sender.sendMessage("§a成功删除游戏 " + gameName);
        }
    }
    
    private void handleList(CommandSender sender) {
        Map<String, CSGame> games = plugin.getGameManager().getGames();
        if (games.isEmpty()) {
            sender.sendMessage("§e目前没有任何游戏");
            return;
        }
        
        sender.sendMessage("§6========= 游戏列表 =========");
        for (CSGame game : games.values()) {
            sender.sendMessage("§e- " + game.getName() + " §7(玩家数: " + 
                game.getPlayerCount() + ", 状态: " + game.getState() + ")");
        }
        sender.sendMessage("§6==========================");
    }
    
    private void handleInfo(CommandSender sender, String gameName) {
        CSGame game = plugin.getGameManager().getGames().get(gameName);
        if (game == null) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }
        
        sender.sendMessage("§6========= 游戏信息 " + gameName + " =========");
        sender.sendMessage("§e状态: §f" + game.getState());
        sender.sendMessage("§e玩家数: §f" + game.getPlayerCount());
        sender.sendMessage("§eT队分数: §f" + game.getTeamScore(Team.TERRORIST));
        sender.sendMessage("§eCT队分数: §f" + game.getTeamScore(Team.COUNTER_TERRORIST));
        sender.sendMessage("§e当前回合: §f" + game.getRoundNumber());
        sender.sendMessage("§6===================================");
    }
    
    private void handleSetSpawn(CommandSender sender, String gameName, String team) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行！");
            return;
        }
        
        CSGame game = plugin.getGameManager().getGames().get(gameName);
        if (game == null) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }
        
        Player player = (Player) sender;
        Location location = player.getLocation();
        
        if (team.equalsIgnoreCase("t")) {
            game.setSpawnPoint(Team.TERRORIST, location);
            sender.sendMessage("§a已设置 " + gameName + " 的T队出生点");
        } else if (team.equalsIgnoreCase("ct")) {
            game.setSpawnPoint(Team.COUNTER_TERRORIST, location);
            sender.sendMessage("§a已设置 " + gameName + " 的CT队出生点");
        } else {
            sender.sendMessage("§c无效的队伍，请使用 t 或 ct");
        }
    }
    
    private void handleSetBombSite(CommandSender sender, String gameName, String site, double radius) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行！");
            return;
        }
        
        CSGame game = plugin.getGameManager().getGames().get(gameName);
        if (game == null) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }
        
        Player player = (Player) sender;
        Location location = player.getLocation();
        
        if (site.equalsIgnoreCase("A")) {
            game.setBombSite("A", location, radius);
            sender.sendMessage("§a已设置 " + gameName + " 的A点炸弹安放点");
        } else if (site.equalsIgnoreCase("B")) {
            game.setBombSite("B", location, radius);
            sender.sendMessage("§a已设置 " + gameName + " 的B点炸弹安放点");
        } else {
            sender.sendMessage("§c无效的炸弹点，请使用 A 或 B");
        }
    }
    
    private void handleSettings(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c用法: /cmcsadmin settings <选项> <值>");
            sender.sendMessage("§e可用选项:");
            sender.sendMessage("§f- money: 设置初始金钱 (800-16000)");
            sender.sendMessage("§f- time: 设置回合时间 (60-300秒)");
            sender.sendMessage("§f- score: 设置胜利分数 (4-30)");
            return;
        }
        
        String option = args[1].toLowerCase();
        int value;
        try {
            value = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c无效的数值");
            return;
        }
        
        // 添加值范围验证
        switch (option) {
            case "money":
                if (value < 800 || value > 16000) {
                    sender.sendMessage("§c初始金钱必须在 800-16000 之间");
                    return;
                }
                plugin.getConfigManager().setStartMoney(value);
                sender.sendMessage("§a已设置初始金钱为: $" + value);
                break;
            case "time":
                if (value < 60 || value > 300) {
                    sender.sendMessage("§c回合时间必须在 60-300 秒之间");
                    return;
                }
                plugin.getConfigManager().setRoundTime(value);
                sender.sendMessage("§a已设置回合时间为: " + value + "秒");
                break;
            case "score":
                if (value < 4 || value > 30) {
                    sender.sendMessage("§c胜利分数必须在 4-30 之间");
                    return;
                }
                plugin.getConfigManager().setWinScore(value);
                sender.sendMessage("§a已设置胜利分数为: " + value);
                break;
            default:
                sender.sendMessage("§c未知选项");
                break;
        }
    }
    
    private void handleReload(CommandSender sender) {
        try {
            plugin.reloadConfig();
            plugin.getConfigManager().reloadConfigs();
            plugin.getGameManager().reloadGames();
            sender.sendMessage("§a配置重载完成");
        } catch (Exception e) {
            sender.sendMessage("§c配置重载失败: " + e.getMessage());
            plugin.getLogger().severe("配置重载失败: " + e.getMessage());
        }
    }
    
    private void handleStart(CommandSender sender, String gameName) {
        CSGame game = plugin.getGameManager().getGames().get(gameName);
        if (game == null) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }
        
        if (game.forceStart()) {
            sender.sendMessage("§a已强制开始游戏 " + gameName);
        } else {
            sender.sendMessage("§c无法开始游戏，可能玩家数量不足或游戏已在进行中");
        }
    }
    
    private void handleStop(CommandSender sender, String gameName) {
        CSGame game = plugin.getGameManager().getGames().get(gameName);
        if (game == null) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }
        
        if (game.forceStop()) {
            sender.sendMessage("§a已强制停止游戏 " + gameName);
        } else {
            sender.sendMessage("§c无法停止游戏，游戏可能已经停止");
        }
    }
    
    private void handleSetLobby(CommandSender sender, String gameName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行！");
            return;
        }
        
        CSGame game = plugin.getGameManager().getGames().get(gameName);
        if (game == null) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }
        
        Player player = (Player) sender;
        Location location = player.getLocation();
        
        game.setLobbyLocation(location);
        sender.sendMessage("§a已设置 " + gameName + " 的大厅位置");
    }
    
    private void handleSetBuyZone(CommandSender sender, String gameName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行！");
            return;
        }
        
        CSGame game = plugin.getGameManager().getGames().get(gameName);
        if (game == null) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }
        
        Player player = (Player) sender;
        Location location = player.getLocation();
        
        game.setBuyZone(location);
        sender.sendMessage("§a已设置 " + gameName + " 的购买区");
    }
    
    private void handleReset(CommandSender sender, String gameName) {
        CSGame game = plugin.getGameManager().getGames().get(gameName);
        if (game == null) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }
        
        game.reset();
        sender.sendMessage("§a已重置 " + gameName + " 的所有设置");
    }
    
    private void handleDebug(CommandSender sender, String gameName) {
        CSGame game = plugin.getGameManager().getGames().get(gameName);
        if (game == null) {
            sender.sendMessage("§c找不到名为 " + gameName + " 的游戏");
            return;
        }

        sender.sendMessage("§6========= 游戏调试信息 " + gameName + " =========");
        sender.sendMessage("§e状态: §f" + game.getState());
        sender.sendMessage("§e玩家数: §f" + game.getPlayerCount());
        sender.sendMessage("§eT队人数: §f" + game.getTeamCount(Team.TERRORIST));
        sender.sendMessage("§eCT队人数: §f" + game.getTeamCount(Team.COUNTER_TERRORIST));
        sender.sendMessage("§e回合数: §f" + game.getRoundNumber());
        sender.sendMessage("§e炸弹状态: §f" + (game.isBombPlanted() ? "已安放" : "未安放"));
        sender.sendMessage("§e购买区状态: §f" + (game.hasBuyZone() ? "已设置" : "未设置"));
        sender.sendMessage("§e出生点状态:");
        sender.sendMessage("§7  - T: " + (game.hasSpawnPoint(Team.TERRORIST) ? "已设置" : "未设置"));
        sender.sendMessage("§7  - CT: " + (game.hasSpawnPoint(Team.COUNTER_TERRORIST) ? "已设置" : "未设置"));
        sender.sendMessage("§6=====================================");
    }
    
    private void handleBackup(CommandSender sender) {
        plugin.backupGames();
        sender.sendMessage("§a已备份所有游戏数据");
    }
}