package WLYD.cloudMist_CS.game;

public enum GameState {
    WAITING,        // 等待开始
    WARMUP,         // 热身时间
    STARTING,       // 准备开始
    FREEZE_TIME,    // 回合开始冻结时间
    BUY_TIME,       // 购买时间
    IN_PROGRESS,    // 回合进行中
    ROUND_END,      // 回合结束
    ENDING,         // 游戏结束
    PAUSED          // 游戏暂停
} 