package nivek71.api.minigame.lobby.rotations;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.MiniGameInfo;
import nivek71.api.minigame.lobby.Lobby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MiniGameRotationCycle extends MiniGameRotation {
    private List<MiniGameInfo<?, ?>> miniGameList = new ArrayList<>();
    private int miniGameIndex = -1;

    public MiniGameRotationCycle(Collection<? extends MiniGameInfo<?, ?>> availableGames) {
        miniGameList.addAll(availableGames);
    }

    public MiniGameRotationCycle(MiniGameInfo<?, ?>... availableGames) {
        this(Arrays.asList(availableGames));
    }

    @Override
    protected List<MiniGameInfo<?, ?>> getRankedMiniGames() {
        List<MiniGameInfo<?, ?>> miniGameList = new ArrayList<>(getAvailableMiniGames());
        List<MiniGameInfo<?, ?>> ranked = new ArrayList<>(miniGameList.subList(miniGameIndex, miniGameList.size()));
        ranked.addAll(miniGameList.subList(0, miniGameIndex));
        return ranked;
    }

    @Override
    public Collection<MiniGameInfo<?, ?>> getAvailableMiniGames() {
        return miniGameList.isEmpty() ? MiniGameInfo.getMiniGameTypes() : miniGameList;
    }

    public int getMiniGameIndex() {
        return miniGameIndex;
    }

    public void setMiniGameIndex(int miniGameIndex) {
        this.miniGameIndex = miniGameList.isEmpty() ? 0 : miniGameIndex % miniGameList.size();
    }

    @Override
    public MiniGame getNextMiniGame(Lobby lobby) {
        setMiniGameIndex(miniGameIndex + 1);
        return super.getNextMiniGame(lobby);
    }
}
