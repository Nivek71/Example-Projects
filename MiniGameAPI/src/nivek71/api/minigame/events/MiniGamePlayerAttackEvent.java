package nivek71.api.minigame.events;

import nivek71.api.minigame.player.MiniGamePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class MiniGamePlayerAttackEvent extends MiniGamePlayerEvent implements Cancellable {
    private static HandlerList handlerList = new HandlerList();
    private final LivingEntity damaged;
    private double damage;
    private boolean cancelled;

    public MiniGamePlayerAttackEvent(MiniGamePlayer player, LivingEntity damaged, double damage) {
        super(player);
        this.damaged = damaged;
        this.damage = damage;
    }

    public double getDamage() {
        return damage;
    }

    public LivingEntity getDamaged() {
        return damaged;
    }

    public void setDamage(int damage) {
        this.damage = damage;
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
