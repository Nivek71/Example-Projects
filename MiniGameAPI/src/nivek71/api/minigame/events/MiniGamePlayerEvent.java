package nivek71.api.minigame.events;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.player.MiniGamePlayer;
import org.bukkit.event.player.PlayerEvent;

public abstract class MiniGamePlayerEvent extends PlayerEvent {
    private final MiniGamePlayer player;

    public MiniGamePlayerEvent(MiniGamePlayer player) {
        super(player.getPlayer());
        this.player = player;
    }

    public MiniGamePlayer getMiniGamePlayer() {
        return player;
    }

    public Lobby getLobby() {
        return player.getLobby();
    }

    public MiniGame getMiniGame() {
        return getLobby().getMiniGame();
    }
}
