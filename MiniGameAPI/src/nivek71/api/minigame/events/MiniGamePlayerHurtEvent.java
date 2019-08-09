package nivek71.api.minigame.events;

import nivek71.api.minigame.player.MiniGamePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class MiniGamePlayerHurtEvent extends MiniGamePlayerEvent implements Cancellable {
    private static HandlerList handlerList = new HandlerList();
    private double damage;
    private boolean cancelled;

    public MiniGamePlayerHurtEvent(MiniGamePlayer player, double damage) {
        super(player);
        this.damage = damage;
    }

    public double getDamage() {
        return damage;
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
