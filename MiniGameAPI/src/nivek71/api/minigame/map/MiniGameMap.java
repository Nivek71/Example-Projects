package nivek71.api.minigame.map;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.MiniGameInfo;
import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.events.MapMadeAvailableEvent;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.Logger;
import nivek71.api.utility.area.BoxedArea;
import nivek71.api.utility.rule.RuleBound;
import nivek71.api.utility.rule.RuleBoundLink;
import nivek71.api.utility.rule.rules.Rule;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class MiniGameMap extends BoxedArea implements RuleBoundLink {
    public static final int DEFAULT_CHUNK_REBUILD_PER_CYCLE = 10;
    public static final int DEFAULT_CHUNK_REBUILD_PERIOD = 1;
    private static final Set<MiniGameMap> availableMapList = new HashSet<>();
    private static final Set<MiniGameMap> unavailableMapList = new HashSet<>();

    private final Map<Rule, Boolean> ruleBounds = new HashMap<>();
    private final String mapName;
    List<MapConfiguration<?>> mapConfigurationList = new ArrayList<>();
    private Lobby claimedLobby;
    private MapConfiguration<?> activeConfiguration;
    private Location spectatorLocation;
    private Collection<Chunk> unfinishedChunks = new ArrayList<>();
    private BukkitTask rebuildingTask;
    private int fireworkLaunchY;

    public MiniGameMap(String mapName, Location corner1, Location corner2, Location spectatorLocation, int fireworkLaunchY) {
        super(corner1, corner2);
        this.mapName = mapName;
        this.spectatorLocation = spectatorLocation;
        this.fireworkLaunchY = fireworkLaunchY;
        availableMapList.add(this);

        spectatorLocation.getWorld().setAutoSave(false); // not sure if this works; autosave in bukkit.yml MUST be set to -1
    }

    public MiniGameMap(String mapName, Location corner1, Location corner2, Location spectatorLocation) {
        this(mapName, corner1, corner2, spectatorLocation, corner1.getBlockY());
    }

    public String getName() {
        return mapName;
    }

    public static Collection<MiniGameMap> getAvailableMapList() {
        return availableMapList;
    }

    public static Collection<MiniGameMap> getUnavailableMapList() {
        return unavailableMapList;
    }

    public Lobby getClaimedLobby() {
        return claimedLobby;
    }

    public MapConfiguration getActiveConfiguration() {
        return activeConfiguration;
    }

    private void makeAvailable() {
        availableMapList.add(this);
        unavailableMapList.remove(this);
        MapMadeAvailableEvent event = new MapMadeAvailableEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void claimMap(MiniGame<?> miniGame) {
        Validate.notNull(miniGame, "miniGame cannot be null");
        Validate.isTrue(miniGame.getMapConfiguration().getMap() == this, "Map configuration is not handled by this map");
        // if map is available, or map is reserved by this lobby
        if (availableMapList.remove(this)) {
            activeConfiguration = miniGame.getMapConfiguration();
            claimedLobby = miniGame.getLobby();
            unavailableMapList.add(this);
        } else throw new IllegalStateException("Cannot claim Map; map has already been claimed");
    }

    public void releaseMap(Lobby safetyCheck) {
        Validate.isTrue(claimedLobby == safetyCheck, "Cannot unclaim; given lobby is not owner");
        if (activeConfiguration != null) {
            // call onMapReset in valid state
            Logger.tryOrLog(() -> activeConfiguration.onMapReset());
        }

        claimedLobby = null;

        if (activeConfiguration != null) {
            boolean allowDamage = activeConfiguration.allowMapDamage();
            activeConfiguration = null;
            if (allowDamage)
                rebuildMap();
                // makeAvailable may modify state; must be final line
            else makeAvailable();
        }
    }

    public boolean isClaimed() {
        return claimedLobby != null;
    }

    public boolean isAvailable() {
        return !isClaimed() && !isRebuilding() && (unfinishedChunks == null || unfinishedChunks.isEmpty());
    }

    public <T extends MiniGame<S>, S extends MapConfiguration<T>> List<MapConfiguration<? extends T>> getAvailableMapConfigurationFor(MiniGameInfo<T, S> miniGameInfo) {
        List<MapConfiguration<? extends T>> availableConfigurations = new ArrayList<>();
        for (MapConfiguration<?> configuration : mapConfigurationList)
            if (configuration.getRequiredMiniGameType().isAssignableFrom(miniGameInfo.getMiniGameClass()))
                availableConfigurations.add(Helpers.helper(configuration));
        return availableConfigurations;
    }

    public boolean isDamageable() {
        return activeConfiguration != null && activeConfiguration.allowMapDamage();
    }

    public Location getSpectatorStartLocation() {
        return spectatorLocation;
    }

    public void setSpectatorLocation(Location spectatorLocation) {
        this.spectatorLocation = spectatorLocation;
    }

    public int getFireworkLaunchY() {
        return fireworkLaunchY;
    }

    public void setFireworkLaunchY(int fireworkLaunchY) {
        this.fireworkLaunchY = fireworkLaunchY;
    }

    // auto-save in bukkit.yml MUST be set to -1 for this to work
    public static BukkitTask rebuildMap(Iterator<Chunk> chunkIterator, int chunksPerCycle, int period, Consumer<Collection<Chunk>> callback) {
        if (!chunkIterator.hasNext())
            return null;

        List<Chunk> failedChunks = new ArrayList<>();
        return Helpers.runTaskTimer(MiniGamePlugin.getPlugin(), () -> {
            for (int i = 0; i < chunksPerCycle; i++) {
                if (chunkIterator.hasNext()) {
                    Chunk next = chunkIterator.next();
                    if (!next.unload(false))
                        failedChunks.add(next);
                } else {
                    callback.accept(failedChunks);
                    return false;
                }
            }
            return true;
        }, 0, period);
    }

    private void rebuildMap(Iterator<Chunk> iterator, int chunksPerCycle, int period) {
        if (isRebuilding())
            return;
        rebuildingTask = rebuildMap(iterator, chunksPerCycle, period, chunks -> {
            unfinishedChunks = chunks;
            rebuildingTask = null;

            if (unfinishedChunks.isEmpty()) {
                // this map is once again available
                makeAvailable();
            } else
                // try again in three minutes (will try indefinitely many times until completed)
                Helpers.runLater(MiniGamePlugin.getPlugin(), () -> rebuildUnfinishedChunks(DEFAULT_CHUNK_REBUILD_PER_CYCLE, DEFAULT_CHUNK_REBUILD_PERIOD), 3600);
        });
    }

    public void rebuildMap(int chunksPerCycle, int period) {
        rebuildMap(getChunksInArea(), chunksPerCycle, period);
    }

    public void rebuildMap() {
        rebuildMap(DEFAULT_CHUNK_REBUILD_PER_CYCLE, DEFAULT_CHUNK_REBUILD_PERIOD);
    }

    public void rebuildUnfinishedChunks(int chunksPerCycle, int period) {
        rebuildMap(unfinishedChunks.iterator(), chunksPerCycle, period);
    }

    public boolean isRebuilding() {
        return rebuildingTask != null;
    }

    public Collection<Chunk> getUnfinishedChunks() {
        return unfinishedChunks;
    }

    public boolean rebuildingSuccessful() {
        return !isRebuilding() && unfinishedChunks.isEmpty();
    }

    public void cancelRebuilding() {
        if (isRebuilding())
            rebuildingTask.cancel();
    }

    @Override
    public RuleBound getParentBound() {
        return claimedLobby == null ? RuleBound.EMPTY_BOUNDS : claimedLobby.getMiniGame();
    }

    @Override
    public Map<Rule, Boolean> getAllBoundsHere() {
        return ruleBounds;
    }
}
