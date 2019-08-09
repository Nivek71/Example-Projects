package nivek71.api.minigame.map.configurations.teamdivider;

import nivek71.api.minigame.map.MiniGameTeam;
import nivek71.api.minigame.player.MiniGamePlayer;

import java.util.Collection;
import java.util.Iterator;

public interface TeamDivider<T extends MiniGameTeam<?>> extends Iterable<T> {
    void registerTeam(T team);
    void dividePlayers(Collection<Collection<MiniGamePlayer>> playerGroups, Collection<MiniGamePlayer> soloPlayers);
    int getRegisteredTeamsPlayerCount();
    Collection<T> getRegisteredTeams();
    void unregisterTeam(T team);

    @Override
    default Iterator<T> iterator() {
        return getRegisteredTeams().iterator();
    }

    static int getPlayerAmount(Collection<Collection<MiniGamePlayer>> playerGroups, Collection<MiniGamePlayer> soloPlayers) {
        int size = 0;
        for (Collection<MiniGamePlayer> group : playerGroups)
            size += group.size();
        return size + soloPlayers.size();
    }
}
