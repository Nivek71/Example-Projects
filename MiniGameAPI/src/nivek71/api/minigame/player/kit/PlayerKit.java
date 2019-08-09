package nivek71.api.minigame.player.kit;

import nivek71.api.minigame.player.MiniGamePlayer;

public class PlayerKit {
    private final PlayerKitType<?> kitType;

    public PlayerKit(PlayerKitType<?> kitType) {
        this.kitType = kitType;
    }

    public PlayerKitType<?> getType() {
        return kitType;
    }

    public void removeFrom(MiniGamePlayer miniGamePlayer) {
        // do nothing by default
    }

    public void giveTo(MiniGamePlayer miniGamePlayer) {
        // do nothing by default
    }
}
