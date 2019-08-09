package nivek71.api.minigame.events;

import nivek71.api.minigame.player.DamageProfile;
import nivek71.api.minigame.player.MiniGamePlayer;

public class MiniGamePlayerDeathEvent extends MiniGamePlayerHurtEvent {
    private final DamageProfile.DamageEntry damageEntry;
    private int reSpawnDelay;

    public MiniGamePlayerDeathEvent(MiniGamePlayer player, DamageProfile.DamageEntry damageEntry, int reSpawnDelay) {
        super(player, damageEntry.getDamage());
        this.damageEntry = damageEntry;
        this.reSpawnDelay = reSpawnDelay;
    }

    public int getReSpawnDelay() {
        return reSpawnDelay;
    }

    public void setReSpawnDelay(int reSpawnDelay) {
        this.reSpawnDelay = reSpawnDelay;
    }

    public DamageProfile.DamageEntry getDamageEntry() {
        return damageEntry;
    }
}
