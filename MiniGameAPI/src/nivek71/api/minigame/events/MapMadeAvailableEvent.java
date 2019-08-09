package nivek71.api.minigame.events;

import nivek71.api.minigame.map.MiniGameMap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MapMadeAvailableEvent extends Event {
    private static HandlerList handlerList = new HandlerList();
    private final MiniGameMap map;

    public MapMadeAvailableEvent(MiniGameMap map) {
        this.map = map;
    }

    public MiniGameMap getMap() {
        return map;
    }

    public boolean isStillAvailable() {
        return map.isAvailable();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
