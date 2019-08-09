package nivek71.api.minigame.events;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.lobby.Lobby;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LobbyChangeMiniGameEvent extends Event {
    private static HandlerList handlerList = new HandlerList();
    private final MiniGame<?> previous, next;

    public LobbyChangeMiniGameEvent(MiniGame<?> previous, MiniGame<?> next) {
        this.previous = previous;
        this.next = next;
    }

    public Lobby getLobby() {
        return previous.getLobby();
    }

    public MiniGame<?> getPreviousMiniGame() {
        return previous;
    }

    public MiniGame<?> getNextMiniGame() {
        return next;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
