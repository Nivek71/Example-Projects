package nivek71.api.minigame;

import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.lobby.lobbystates.EndingState;
import nivek71.api.minigame.map.MapConfiguration;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Logger;
import nivek71.api.utility.ScoreboardBase;
import nivek71.api.utility.rule.RuleBound;
import nivek71.api.utility.rule.RuleBoundLinkBase;
import org.apache.commons.lang.Validate;

import java.util.Collection;

public abstract class MiniGame<MapConfigurationType extends MapConfiguration<?>> extends RuleBoundLinkBase {
    private final Lobby lobby;
    private MiniGameInfo info;
    private final MapConfigurationType configuration;
    private boolean ended = false;

    private RuleBoundLinkBase spectatorBounds = new RuleBoundLinkBase() {
        @Override
        public RuleBound getParentBound() {
            return getLobby().getLobbyState();
        }
    };

    public MiniGame(Lobby lobby, MiniGameInfo info, MapConfigurationType configuration) {
        Validate.isTrue(configuration.getRequiredMiniGameType().isAssignableFrom(getClass()), "Map configuration does not support miniGame");
        this.lobby = lobby;
        // clone info so it may be changed for this MiniGame only (state of game may change without affecting other instances)
        this.info = info.clone();
        this.configuration = configuration;
    }

    public final void endGame(MiniGameWinner winner) {
        if (ended)
            return;
        ended = true;

        if (winner == null)
            winner = MiniGameWinner.NOBODY;
        {
            final MiniGameWinner finalWinner = winner;
            Logger.tryOrLog(() -> onEnd(finalWinner));
        }

        getLobby().broadcast(winner.getColor() + "---<>---", winner.getColor().toString(), winner.getColor() +
                winner.getName() + " won the game!", winner.getColor().toString(), winner.getColor() + "---<>---");

        lobby.setLobbyState(new EndingState(getLobby()));
    }

    public final void endGame() {
        // if getCurrentlyWinning throws an exception, use nobody (null)
        endGame(Logger.fetchOrLog(this::getCurrentlyWinning));
    }

    /**
     * @return a clear winner, if there is one, or {@link MiniGameWinner#NOBODY} (or null) if there is not
     */
    protected MiniGameWinner getCurrentlyWinning() {
        Collection<? extends MiniGameWinner> winners = configuration.getActiveParticipants();
        return winners.size() == 1 ? winners.iterator().next() : MiniGameWinner.NOBODY;
    }

    public void addPlayerToMiniGame(MiniGamePlayer player) {
        // do nothing by default
    }

    public void removePlayerFromMiniGame(MiniGamePlayer player) {
        // do nothing by default
    }

    public void onStart() {
        checkEnoughParticipants();
    }

    // end may not be called if a game is not ended properly (with #endGame) but instead the InGameState is swapped out
    // unexpectedly
    protected void onEnd(MiniGameWinner winner) {
        // do nothing by default
    }

    // stop is always called after MiniGame has ended
    public void onStop() {
        // do nothing by default
    }

    public int getGameTimeoutSeconds() {
        return 1200; // 20 minutes by default
    }

    public int getGameTimeoutWarningSeconds() {
        // 3/4ths game timeout by default
        return (int) (3.0 / 4 * getGameTimeoutSeconds());
    }

    public void onGameTimeoutWarning() {
        // do nothing by default
    }

    public void onGameTimeout() {
        // do nothing by default
    }

    @Override
    public RuleBound getParentBound() {
        return lobby.getLobbyState();
    }

    public Lobby getLobby() {
        return lobby;
    }

    public MiniGameInfo getInfo() {
        return info;
    }

    public MapConfigurationType getMapConfiguration() {
        return configuration;
    }

    public boolean hasEnoughParticipants() {
        // by default, there should be at least two
        return configuration.getActiveParticipants().size() > 1;
    }

    public ScoreboardBase getScoreboardFor(MiniGamePlayer player) {
        return null; // no scoreboard by default
    }

    public RuleBoundLinkBase getSpectatorBounds() {
        return spectatorBounds;
    }

    public void checkEnoughParticipants() {
        if (!hasEnoughParticipants())
            endGame();
    }
}
