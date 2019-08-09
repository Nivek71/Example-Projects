package nivek71.api.minigame.lobby;

import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.rule.RuleBound;
import nivek71.api.utility.rule.RuleBoundLinkBase;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.lang.annotation.Annotation;

public abstract class LobbyState extends RuleBoundLinkBase implements Listener {
    private final Lobby lobby;

    protected LobbyState(Lobby lobby) {
        this.lobby = lobby;
    }

    public Lobby getLobby() {
        return lobby;
    }

    @Override
    public RuleBound getParentBound() {
        return lobby;
    }

    protected void switchIn(LobbyState previous) {
        Bukkit.getPluginManager().registerEvents(this, MiniGamePlugin.getPlugin());
    }

    protected void switchOut(LobbyState to) {
        HandlerList.unregisterAll(this);
    }

    protected void onPlayerJoin(MiniGamePlayer player) {
        // do nothing by default
    }

    protected void onPlayerQuit(MiniGamePlayer player) {
        // do nothing by default
    }

    public boolean supports(Class<? extends Annotation> annotation) {
        return getClass().isAnnotationPresent(annotation);
    }
}
