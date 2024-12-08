package WLYD.cloudMist_CS.commands;

import WLYD.cloudMist_CS.CloudMist_CS;

public class CommandManager {
    private final PlayerCommand playerCommand;
    private final AdminCommand adminCommand;
    
    public CommandManager(CloudMist_CS plugin) {
        this.playerCommand = new PlayerCommand(plugin);
        this.adminCommand = new AdminCommand(plugin);
    }
    
    public PlayerCommand getPlayerCommand() {
        return playerCommand;
    }
    
    public AdminCommand getAdminCommand() {
        return adminCommand;
    }
} 