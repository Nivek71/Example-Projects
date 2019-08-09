package nivek71.api.minigame.map;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.MiniGameWinner;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.rule.RuleBound;
import nivek71.api.utility.rule.RuleBoundLinkBase;
import nivek71.api.utility.timer.Timer;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MiniGameTeam<T extends MapConfiguration<?>> extends RuleBoundLinkBase implements MiniGameWinner {
    private List<Location> spawnLocations = new ArrayList<>();
    private int spawnLocationIndex = 0;
    private Collection<MiniGamePlayer> miniGamePlayers = new HashSet<>();
    private Collection<MiniGamePlayer> activePlayers = new HashSet<>();
    private T mapConfiguration;
    private final String name;
    private final ChatColor teamColor;

    public MiniGameTeam(String name, ChatColor teamColor, Location... spawnLocations) {
        this.name = name;
        this.teamColor = teamColor;
        this.spawnLocations.addAll(Arrays.asList(spawnLocations));
    }

    public void addPlayers(Collection<MiniGamePlayer> players) {
        for (MiniGamePlayer player : players) {
            miniGamePlayers.add(player);
            activePlayers.add(player);
            player.getPlayer().setPlayerListName(teamColor + player.getName());
        }
    }

    public void addPlayers(MiniGamePlayer... player) {
        addPlayers(Arrays.asList(player));
    }

    public void eliminate(MiniGamePlayer player) {
        activePlayers.remove(player);
    }

    public void removePlayer(MiniGamePlayer player) {
        miniGamePlayers.remove(player);
        activePlayers.remove(player);
        player.getPlayer().setPlayerListName(player.getName());
    }

    public boolean isOnTeam(MiniGamePlayer player) {
        return miniGamePlayers.contains(player);
    }

    public boolean isActiveParticipant(MiniGamePlayer player) {
        return activePlayers.contains(player);
    }

    @Override
    public Collection<MiniGamePlayer> getPlayers() {
        return miniGamePlayers;
    }

    public int getPlayerCount() {
        return miniGamePlayers.size();
    }

    public Collection<MiniGamePlayer> getActivePlayers() {
        return activePlayers;
    }

    public int getActivePlayerCount() {
        return activePlayers.size();
    }

    public T getMapConfiguration() {
        return mapConfiguration;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ChatColor getColor() {
        return teamColor;
    }

    @Override
    public boolean isParticipating() {
        return !activePlayers.isEmpty();
    }

    public List<Location> getSpawnLocations() {
        return spawnLocations;
    }

    public void addSpawnLocation(Location location) {
        spawnLocations.add(location);
    }

    public int getSpawnLocationIndex() {
        return spawnLocationIndex;
    }

    public void setSpawnLocationIndex(int spawnLocationIndex) {
        this.spawnLocationIndex = spawnLocationIndex;
    }

    public Location getNextSpawnLocation() {
        Validate.notEmpty(spawnLocations, "spawnLocations is empty");
        return spawnLocations.get(spawnLocationIndex = (spawnLocationIndex + 1) % spawnLocations.size());
    }

    public void prepareTeam(T mapConfiguration) {
        this.mapConfiguration = mapConfiguration;
    }

    public  void startTeam(T mapConfiguration) {
    }

    public void onMiniGameEnd(MiniGame<T> miniGame) {
        Timer.cancelAssociated(this);
        spawnLocationIndex = 0;
        miniGamePlayers.clear();
        activePlayers.clear();
        mapConfiguration = null;
    }

    public void onMapReset() {
        // do nothing by default
    }

    @Override
    public RuleBound getParentBound() {
        return mapConfiguration;
    }
}
