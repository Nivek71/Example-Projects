package nivek71.api.minigame.lobby.lobbystates;

import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.lobby.LobbyState;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.Logger;
import nivek71.api.utility.timer.Countdown;
import nivek71.api.utility.timer.Timer;

public class InGameState extends ActiveMiniGameStateBase {
    private Countdown gameTimeoutCountdown, gameTimeoutWarningCountdown;

    protected InGameState(Lobby lobby) {
        super(lobby);
    }

    @Override
    public void addPlayerToGame(MiniGamePlayer player) {
        super.addPlayerToGame(player);
    }

    @Override
    protected void switchIn(LobbyState previous) {
        super.switchIn(previous);
        gameTimeoutWarningCountdown = new Countdown(MiniGamePlugin.getPlugin(), getLobby().getMiniGame().getGameTimeoutWarningSeconds(), true, getLobby(), this) {
            @Override
            protected void onEnd() {
                Logger.tryOrLog(() -> getLobby().getMiniGame().onGameTimeoutWarning());
            }
        };
        gameTimeoutCountdown = new Countdown(MiniGamePlugin.getPlugin(), getLobby().getMiniGame().getGameTimeoutSeconds(), true, getLobby(), this) {
            @Override
            protected void onEnd() {
                Logger.tryOrLog(() -> getLobby().getMiniGame().onGameTimeout());
                Logger.tryOrLog(() -> getLobby().getMiniGame().endGame());
            }
        };

        for (MiniGamePlayer player : getLobby().getPlayerList()) {
            if (player.isParticipating()) {
                player.setParentBounds(player.getMiniGame().getMapConfiguration());
            }
            // set spectator bounds for all players (for when they become spectators)
            player.setSpectatorBounds(Logger.fetchOrLog(() -> getLobby().getMiniGame().getSpectatorBounds()));
        }

        Logger.tryOrLog(() -> getLobby().getMiniGame().getMapConfiguration().onMiniGameStart(Helpers.helper(getLobby().getMiniGame())));
        Logger.tryOrLog(() -> getLobby().getMiniGame().onStart());
    }

    public int getSecondsUntilGameTimeout() {
        return gameTimeoutCountdown.getRemainingIntervals();
    }

    public int getSecondsUntilGameTimeoutWarning() {
        return gameTimeoutWarningCountdown.getRemainingIntervals();
    }

    @Override
    protected void switchOut(LobbyState to) {
        super.switchOut(to);
        Logger.tryOrLog(() -> getLobby().getMiniGame().onStop());
        Logger.tryOrLog(() -> getLobby().getMiniGame().getMapConfiguration().onMiniGameEnd(Helpers.helper(getLobby().getMiniGame())));
        // cancel timers registered to the miniGame
        Timer.cancelAssociated(getLobby().getMiniGame());

        for (MiniGamePlayer player : getLobby().getPlayerList()) {
            player.setParentBounds(null);
            player.setSpectatorBounds(null);
        }
    }

    @Override
    protected void onPlayerQuit(MiniGamePlayer player) {
        super.onPlayerQuit(player);
        getLobby().getMiniGame().checkEnoughParticipants();
    }
}
