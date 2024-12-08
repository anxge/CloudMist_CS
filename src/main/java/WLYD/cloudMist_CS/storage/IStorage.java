package WLYD.cloudMist_CS.storage;

import java.util.UUID;
import WLYD.cloudMist_CS.data.PlayerData;
import WLYD.cloudMist_CS.data.GameData;

public interface IStorage {
    void init();
    void close();
    boolean isConnected();
    void savePlayerData(UUID playerId, PlayerData data);
    PlayerData loadPlayerData(UUID playerId);
    void saveGameData(String gameName, GameData data);
    GameData loadGameData(String gameName);
} 