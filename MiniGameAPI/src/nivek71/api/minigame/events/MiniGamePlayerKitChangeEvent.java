package nivek71.api.minigame.events;

import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.minigame.player.kit.PlayerKit;
import nivek71.api.minigame.player.kit.PlayerKitType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class MiniGamePlayerKitChangeEvent extends MiniGamePlayerEvent implements Cancellable {
    private static HandlerList handlerList = new HandlerList();
    private boolean cancelled;
    private final PlayerKit previous;
    private PlayerKitType<?> nextKitType;
    private boolean changeNow;

    public MiniGamePlayerKitChangeEvent(MiniGamePlayer player, PlayerKit previous, PlayerKitType<?> nextKitType, boolean changeNow) {
        super(player);
        this.previous = previous;
        this.nextKitType = nextKitType;
        this.changeNow = changeNow;
    }

    public PlayerKit getPreviousKit() {
        return previous;
    }

    public PlayerKitType<?> getNextKitType() {
        return nextKitType;
    }

    public void setNextKitType(PlayerKitType<?> nextKitType) {
        this.nextKitType = nextKitType;
    }

    public boolean isChangeNow() {
        return changeNow;
    }

    public void setChangeNow(boolean changeNow) {
        this.changeNow = changeNow;
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
