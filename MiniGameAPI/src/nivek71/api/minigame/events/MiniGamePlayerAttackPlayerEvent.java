package nivek71.api.minigame.events;

import nivek71.api.minigame.player.MiniGamePlayer;
import org.bukkit.entity.Player;

public class MiniGamePlayerAttackPlayerEvent extends MiniGamePlayerAttackEvent {
    private final MiniGamePlayer damaged;

    public MiniGamePlayerAttackPlayerEvent(MiniGamePlayer player, MiniGamePlayer damaged, double damage) {
        super(player, damaged.getPlayer(), damage);
        this.damaged = damaged;
    }

    public MiniGamePlayer getDamagedPlayer() {
        return damaged;
    }

    @Override
    public Player getDamaged() {
        return damaged.getPlayer();
    }
}
