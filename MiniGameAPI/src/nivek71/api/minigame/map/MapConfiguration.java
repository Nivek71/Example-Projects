package nivek71.api.minigame.map;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Logger;
import nivek71.api.utility.rule.RuleBound;
import nivek71.api.utility.rule.RuleBoundLinkBase;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;

import java.util.Collection;

public abstract class MapConfiguration<T extends MiniGame> extends RuleBoundLinkBase {
    private final Class<T> requiredMiniGameType;
    private final MiniGameMap map;
    private final boolean allowMapDamage;

    public MapConfiguration(Class<T> requiredMiniGameType, MiniGameMap map, boolean allowMapDamage) {
        Validate.notNull(requiredMiniGameType, "requiredMiniGameType cannot be null");
        Validate.notNull(map, "map cannot be null");
        this.requiredMiniGameType = requiredMiniGameType;
        this.map = map;
        this.allowMapDamage = allowMapDamage;
        map.mapConfigurationList.add(this);
    }

    public Class<T> getRequiredMiniGameType() {
        return requiredMiniGameType;
    }

    @Override
    public RuleBound getParentBound() {
        return map;
    }

    public MiniGameMap getMap() {
        return map;
    }

    public Lobby getActiveLobby() {
        Validate.isTrue(equals(map.getActiveConfiguration()), "map configuration is not active");
        return map.getClaimedLobby();
    }

    public T getMiniGame() {
        Validate.isTrue(equals(map.getActiveConfiguration()), "map configuration is not active");
        //noinspection unchecked
        return (T) map.getClaimedLobby().getMiniGame();
    }

    public boolean allowMapDamage() {
        return allowMapDamage;
    }

    public void onPrepareMiniGame(T miniGame) {
        // do nothing by default
    }

    public void onMiniGameStart(T miniGame) {
        // do nothing by default
    }

    public void onMiniGameEnd(T miniGame) {
        // do nothing by default
    }

    public void onMapReset() {
        // do nothing by default
    }

    public void onPlayerAdd(MiniGamePlayer player) {
        // do nothing by default
    }

    public void onPlayerAdd(Collection<MiniGamePlayer> players) {
        for (MiniGamePlayer player : players)
            Logger.tryOrLog(() -> onPlayerAdd(player));
    }

    public void onPlayerRemove(MiniGamePlayer player) {
        // do nothing by default
    }

    public abstract Collection<? extends MiniGameTeam<?>> getActiveParticipants();
    public abstract Location getSpawnLocationFor(MiniGamePlayer miniGamePlayer);
}
