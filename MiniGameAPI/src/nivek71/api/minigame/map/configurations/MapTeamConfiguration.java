package nivek71.api.minigame.map.configurations;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.events.MiniGamePlayerDeathEvent;
import nivek71.api.minigame.map.MapConfiguration;
import nivek71.api.minigame.map.MiniGameMap;
import nivek71.api.minigame.map.MiniGameTeam;
import nivek71.api.minigame.map.configurations.teamdivider.TeamDivider;
import nivek71.api.minigame.map.configurations.teamdivider.WeighedTeamDivider;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.containers.Pair;
import nivek71.api.utility.rule.rules.StandardRule;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MapTeamConfiguration<ThisType extends MapTeamConfiguration<ThisType, T, S>, T extends MiniGame<ThisType>, S extends MiniGameTeam<ThisType>> extends MapConfiguration<T> {
    protected final TeamDivider<S> teamDivider;

    static {
        // only needs to register if teams are used
        MiniGamePlugin.registerOnEnableFunction(() -> Helpers.registerDynamicEvent(MiniGamePlugin.getPlugin(), MiniGamePlayerDeathEvent.class, event -> {
            // no reSpawning; eliminate player
            if (event.getReSpawnDelay() < 0 && event.getMiniGame().getMapConfiguration() instanceof MapTeamConfiguration) {
                ((MapTeamConfiguration) event.getMiniGame().getMapConfiguration()).getPlayersTeam(event.getMiniGamePlayer()).eliminate(event.getMiniGamePlayer());
            }
        }, EventPriority.MONITOR, true));
    }

    public MapTeamConfiguration(Class<T> requiredMiniGameType, MiniGameMap map, boolean allowMapDamage, TeamDivider<S> teamDivider) {
        super(requiredMiniGameType, map, allowMapDamage);
        this.teamDivider = teamDivider;
        if (allowMapDamage)
            setRuleState(StandardRule.PLAYER_MODIFY_BLOCK, true);
    }

    @SafeVarargs
    public MapTeamConfiguration(Class<T> requiredMiniGameType, MiniGameMap map, boolean allowDamage, S... teams) {
        this(requiredMiniGameType, map, allowDamage, new WeighedTeamDivider<>());
        for (S team : teams)
            teamDivider.registerTeam(team);
    }

    @Override
    public void onPrepareMiniGame(T miniGame) {
        for (S team : teamDivider)
            // this should always be an acceptable cast; no way to verify, however if it fails it is on the user
            //noinspection unchecked
            team.prepareTeam((ThisType) this);
    }

    private void forEachPlayer(MiniGamePlayer player) {
        player.setParentBounds(getPlayersTeam(player));
    }

    @Override
    public void onMiniGameStart(T miniGame) {
        super.onMiniGameStart(miniGame);
        miniGame.getLobby().getPlayerList().forEach(this::forEachPlayer);
        for (S team : teamDivider)
            //noinspection unchecked
            team.startTeam((ThisType) this);
    }

    @Override
    public void onMiniGameEnd(T miniGame) {
        super.onMiniGameEnd(miniGame);
        for (S team : teamDivider)
            team.onMiniGameEnd(miniGame);
    }

    @Override
    public void onMapReset() {
        super.onMapReset();
        for (S team : teamDivider)
            team.onMapReset();
    }

    @Override
    public void onPlayerAdd(MiniGamePlayer player) {
        super.onPlayerAdd(player);
        Validate.isTrue(teamDivider.iterator().hasNext(), "map configuration must have at least one team");
        teamDivider.dividePlayers(Collections.emptyList(), Collections.singleton(player));
        forEachPlayer(player);
    }

    @Override
    public void onPlayerAdd(Collection<MiniGamePlayer> players) {
        Validate.isTrue(teamDivider.iterator().hasNext(), "map configuration must have at least one team");
        Pair<Collection<Collection<MiniGamePlayer>>, Collection<MiniGamePlayer>> parties = MiniGamePlugin.getPartiesOf(players);
        teamDivider.dividePlayers(parties.getFirst(), parties.getSecond());
    }

    @Override
    public void onPlayerRemove(MiniGamePlayer player) {
        super.onPlayerRemove(player);
        S team = getPlayersTeam(player);
        if (team != null)
            team.removePlayer(player);
    }

    public S getPlayersTeam(MiniGamePlayer player) {
        for (S team : teamDivider) {
            if (team.isOnTeam(player))
                return team;
        }
        return null;
    }

    public Collection<S> getTeams() {
        return teamDivider.getRegisteredTeams();
    }

    @Override
    public Collection<S> getActiveParticipants() {
        List<S> activeTeams = new ArrayList<>(teamDivider.getRegisteredTeams());
        activeTeams.removeIf(team -> !team.isParticipating());
        return activeTeams;
    }

    @Override
    public Location getSpawnLocationFor(MiniGamePlayer miniGamePlayer) {
        S team = getPlayersTeam(miniGamePlayer);
        return team == null ? null : team.getNextSpawnLocation();
    }
}
