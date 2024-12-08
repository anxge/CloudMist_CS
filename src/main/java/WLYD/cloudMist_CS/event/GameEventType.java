package WLYD.cloudMist_CS.event;

public enum GameEventType {
    GAME_START("游戏开始"),
    GAME_END("游戏结束"),
    ROUND_START("回合开始"),
    ROUND_END("回合结束"),
    PLAYER_JOIN("玩家加入"),
    PLAYER_LEAVE("玩家离开"),
    BOMB_PLANTED("炸弹安装"),
    BOMB_DEFUSED("炸弹拆除"),
    TEAM_WIN("队伍胜利");

    private final String displayName;

    GameEventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 