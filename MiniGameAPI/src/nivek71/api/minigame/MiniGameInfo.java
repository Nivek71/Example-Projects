package nivek71.api.minigame;

import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.map.MapConfiguration;
import nivek71.api.minigame.player.kit.KitManager;
import nivek71.api.utility.functions.TriFunctionEx;
import nivek71.api.utility.rule.RuleBoundBase;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

public class MiniGameInfo<T extends MiniGame<? super S>, S extends MapConfiguration<? extends T>> extends RuleBoundBase implements Cloneable {
    private static final List<MiniGameInfo<?, ?>> miniGameTypes = new ArrayList<>();
    private String name;
    private final Class<T> miniGameClass;
    private KitManager kitManager;
    private int minPlayersToStart;
    private final TriFunctionEx<Lobby, MiniGameInfo<T, S>, S, T> miniGameBuilder;

    public MiniGameInfo(String name, Class<T> miniGameClass, int minPlayersToStart, KitManager kitManager,
                        TriFunctionEx<Lobby, MiniGameInfo<T, S>, S, T> miniGameBuilder) {
        this.name = name;
        this.miniGameClass = miniGameClass;
        this.minPlayersToStart = minPlayersToStart;
        this.kitManager = kitManager;
        this.miniGameBuilder = miniGameBuilder;
        miniGameTypes.add(this);
    }

    public static List<MiniGameInfo<?, ?>> getMiniGameTypes() {
        return miniGameTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Validate.notNull(name, "Cannot set name to null");
        this.name = name;
    }

    public Class<T> getMiniGameClass() {
        return miniGameClass;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public void setKitManager(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    public int getMinPlayersToStart() {
        return minPlayersToStart;
    }

    public void setMinPlayersToStart(int minPlayersToStart) {
        this.minPlayersToStart = minPlayersToStart;
    }

    public TriFunctionEx<Lobby, MiniGameInfo<T, S>, S, T> getMiniGameBuilder() {
        return miniGameBuilder;
    }

    public T createInstance(Lobby lobby, S configuration) {
        return miniGameBuilder.apply(lobby, this, configuration);
    }

    @Override
    public MiniGameInfo<T, S> clone() {
        try {
            //noinspection unchecked
            MiniGameInfo<T, S> miniGameInfo = (MiniGameInfo<T, S>) super.clone();
            miniGameInfo.kitManager = kitManager.clone();
            return miniGameInfo;
        } catch (CloneNotSupportedException ex) {
            // should never happen, rethrow exception if (for some bizarre reason) it does
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return "MiniGameInfo{" +
                "name='" + name + '\'' +
                ", miniGameClass=" + miniGameClass +
                '}';
    }
}
