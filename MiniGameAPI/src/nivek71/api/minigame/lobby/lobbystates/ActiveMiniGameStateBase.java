package nivek71.api.minigame.lobby.lobbystates;

import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.lobby.LobbyState;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Logger;
import nivek71.api.utility.ScoreboardBase;

public abstract class ActiveMiniGameStateBase extends LobbyState {
    public ActiveMiniGameStateBase(Lobby lobby) {
        super(lobby);
    }

    void finishAddingPlayer(MiniGamePlayer player) {
        Logger.tryOrLog(() -> player.getPlayer().teleport(getLobby().getMiniGame().getMapConfiguration().getSpawnLocationFor(player)));
        Logger.tryOrLog(() -> getLobby().getMiniGame().addPlayerToMiniGame(player));
        ScoreboardBase base = Logger.fetchOrLog(() -> getLobby().getMiniGame().getScoreboardFor(player));
        if (base == null)
            ScoreboardBase.removeScoreboard(player.getPlayer());
        else
            base.setScoreboardFor(player.getPlayer());
    }

    public void addPlayerToGame(MiniGamePlayer player) {
        player.setSpectator(false);
        Logger.tryOrLog(() -> getLobby().getMiniGame().getMapConfiguration().onPlayerAdd(player));
        finishAddingPlayer(player);
    }

    private void removePlayer(MiniGamePlayer player) {
        Logger.tryOrLog(() -> getLobby().getMiniGame().getMapConfiguration().onPlayerRemove(player));
        Logger.tryOrLog(() -> getLobby().getMiniGame().removePlayerFromMiniGame(player));
        player.removeKit();
    }

    @Override
    protected void switchOut(LobbyState to) {
        super.switchOut(to);
        if (!(to instanceof ActiveMiniGameStateBase)) {
            // if the next state is not an active MiniGame state, move all players out of spectator mode & out of the game
            for (MiniGamePlayer player : getLobby().getPlayerList()) {
                if (player.isSpectator())
                    player.setSpectator(false);
                removePlayer(player);
                ScoreboardBase.removeScoreboard(player.getPlayer());
            }
        }
    }

    @Override
    protected void onPlayerJoin(MiniGamePlayer player) {
        super.onPlayerJoin(player);
        player.setSpectator(true);
        player.getPlayer().teleport(getLobby().getMiniGame().getMapConfiguration().getMap().getSpectatorStartLocation());
    }

    @Override
    protected void onPlayerQuit(MiniGamePlayer player) {
        super.onPlayerQuit(player);
        if (player.isSpectator())
            player.setSpectator(false);
        removePlayer(player);
    }
}
