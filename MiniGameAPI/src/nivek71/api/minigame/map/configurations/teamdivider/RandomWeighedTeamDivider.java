package nivek71.api.minigame.map.configurations.teamdivider;

import nivek71.api.minigame.map.MiniGameTeam;
import nivek71.api.minigame.player.MiniGamePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RandomWeighedTeamDivider<T extends MiniGameTeam<?>> extends WeighedTeamDivider<T> {
    @Override
    public void dividePlayers(Collection<Collection<MiniGamePlayer>> playerGroups, Collection<MiniGamePlayer> soloPlayers) {
        List<MiniGamePlayer> totalPlayers = new ArrayList<>(soloPlayers);
        for(Collection<MiniGamePlayer> group : playerGroups) {
            totalPlayers.addAll(group);
        }
        Collections.shuffle(totalPlayers);
        super.dividePlayers(Collections.emptyList(), totalPlayers);
    }
}
