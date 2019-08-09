package nivek71.api.minigame.events;

import nivek71.api.minigame.player.MiniGamePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class MiniGamePlayerToggleForceSpectatorEvent extends MiniGamePlayerEvent implements Cancellable {
    private static HandlerList handlerList = new HandlerList();
    private final boolean nextState;
    private boolean cancelled;

    public MiniGamePlayerToggleForceSpectatorEvent(MiniGamePlayer player, boolean nextState) {
        super(player);
        this.nextState = nextState;
    }

    public boolean getNextState() {
        return nextState;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
