package nivek71.api.minigame.events;

import nivek71.api.minigame.player.MiniGamePlayer;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;

public class MiniGamePlayerRespawnEvent extends MiniGamePlayerEvent {
    private static HandlerList handlerList = new HandlerList();
    private Location spawnLocation;

    public MiniGamePlayerRespawnEvent(MiniGamePlayer player, Location spawnLocation) {
        super(player);
        this.spawnLocation = spawnLocation;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
