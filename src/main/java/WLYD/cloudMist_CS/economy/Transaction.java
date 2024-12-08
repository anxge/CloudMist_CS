package WLYD.cloudMist_CS.economy;

import java.time.Instant;
import java.util.UUID;

public class Transaction {
    private final UUID playerId;
    private final int amount;
    private final String reason;
    private final Instant timestamp;
    
    public Transaction(UUID playerId, int amount, String reason) {
        this.playerId = playerId;
        this.amount = amount;
        this.reason = reason;
        this.timestamp = Instant.now();
    }
    
    public UUID getPlayerId() { return playerId; }
    public int getAmount() { return amount; }
    public String getReason() { return reason; }
    public Instant getTimestamp() { return timestamp; }
} 