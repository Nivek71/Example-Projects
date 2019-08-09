package nivek71.api.minigame.lobby.rotations;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.MiniGameInfo;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.map.MapConfiguration;
import nivek71.api.minigame.map.MiniGameMap;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.Logger;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class MiniGameRotation {
    public <MiniGameType extends MiniGame<MapConfigurationType>, MapConfigurationType extends MapConfiguration<MiniGameType>>
    MapConfigurationType getConfigurationFor(MiniGameInfo<MiniGameType, MapConfigurationType> info, MiniGameMap map) {
        List<MapConfiguration<? extends MiniGameType>> availableConfigurations = map.getAvailableMapConfigurationFor(info);
        if (!availableConfigurations.isEmpty())
            //noinspection unchecked
            return (MapConfigurationType) availableConfigurations.get((int) (Math.random() * availableConfigurations.size()));
        return null;
    }

    public List<MiniGameMap> getRankedMapsFor(MiniGameInfo<?, ?> miniGameInfo) {
        List<MiniGameMap> maps = new ArrayList<>(getAvailableMapsFor(miniGameInfo));
        Collections.shuffle(maps);
        return maps;
    }

    public Collection<MiniGameMap> getAvailableMapsFor(MiniGameInfo<?, ?> miniGameInfo) {
        List<MiniGameMap> maps = new ArrayList<>(MiniGameMap.getAvailableMapList());
        maps.removeIf(map -> map.getAvailableMapConfigurationFor(Helpers.helper(miniGameInfo)).isEmpty());
        return maps;
    }

    public <MiniGameType extends MiniGame<MapConfigurationType>, MapConfigurationType extends MapConfiguration<MiniGameType>>
    MapConfigurationType getConfigurationFor(MiniGameInfo<MiniGameType, MapConfigurationType> info) {
        List<MiniGameMap> rankedMaps = getRankedMapsFor(info);
        if (rankedMaps.isEmpty())
            return null;
        return getConfigurationFor(info, rankedMaps.get(0));
    }

    public MiniGame getNextMiniGame(Lobby lobby, MiniGameMap map) {
        List<MiniGameInfo<?, ?>> rankedGames = getRankedMiniGames();
        Validate.notEmpty(rankedGames, "Cannot get next MiniGame; no available games provided");
        for (MiniGameInfo<?, ?> info : rankedGames) {
            try {
                MapConfiguration<?> configuration = map == null ? getConfigurationFor(Helpers.helper(info)) : getConfigurationFor(Helpers.helper(info), map);
                if (configuration != null) {
                    // configuration must return a map configuration within bounds
                    // we cannot hold on to those bounds here, however
                    return info.createInstance(lobby, Helpers.helper(configuration));
                }
            } catch (Exception ex) {
                Logger.logException(ex);
            }
        }
        // map configurations not available (or threw error)
        return null;
    }

    public MiniGame getNextMiniGame(Lobby lobby) {
        return getNextMiniGame(lobby, null);
    }

    /**
     * An ordered version of {@link #getAvailableMiniGames()}, this method is used to determine the priority of each
     * game. By default the first entry will be used, unless there is no map configuration available
     *
     * @return a list where elements are ordered based on their likelihood to be played.
     */
    protected List<MiniGameInfo<?, ?>> getRankedMiniGames() {
        List<MiniGameInfo<?, ?>> availableGames = new ArrayList<>(getAvailableMiniGames());
        Collections.shuffle(availableGames);
        return availableGames;
    }

    /**
     * @return a collection of all MiniGame types this rotation may provide
     */
    public abstract Collection<MiniGameInfo<?, ?>> getAvailableMiniGames();
}
