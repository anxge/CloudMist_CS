package WLYD.cloudMist_CS.scoreboard;

import org.bukkit.entity.Player;
import WLYD.cloudMist_CS.game.CSGame;

/**
 * 计分板更新监听器接口
 */
public interface ScoreboardUpdateListener {
    /**
     * 当计分板需要更新时调用
     * @param player 需要更新计分板的玩家
     * @param game 玩家所在的游戏
     */
    void onScoreboardUpdate(Player player, CSGame game);
}
