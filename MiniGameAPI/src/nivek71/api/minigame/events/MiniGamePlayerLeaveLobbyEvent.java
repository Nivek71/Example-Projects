package nivek71.api.minigame.events;

import nivek71.api.minigame.player.MiniGamePlayer;
import org.bukkit.event.HandlerList;

public class MiniGamePlayerLeaveLobbyEvent extends MiniGamePlayerEvent {
    private static HandlerList handlerList = new HandlerList();

    public MiniGamePlayerLeaveLobbyEvent(MiniGamePlayer player) {
        super(player);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
