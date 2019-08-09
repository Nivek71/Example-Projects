package nivek71.api.minigame.lobby.lobbystates;

import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.lobby.LobbyState;
import nivek71.api.minigame.map.MapConfiguration;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.Logger;
import nivek71.api.utility.rule.rules.StandardRule;
import nivek71.api.utility.timer.Countdown;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class StartingState extends ActiveMiniGameStateBase {
    public static final int STARTING_COUNTDOWN_SECONDS = 8;

    public StartingState(Lobby lobby) {
        super(lobby);
        setRuleState(StandardRule.PLAYER_QUEUE_KIT, true);
        setRuleState(StandardRule.PLAYER_APPLY_KIT, true);
    }

    @Override
    protected void switchIn(LobbyState previous) {
        super.switchIn(previous);

        MapConfiguration<?> config = getLobby().getMiniGame().getMapConfiguration();
        Logger.tryOrLog(() -> config.onPrepareMiniGame(Helpers.helper(getLobby().getMiniGame())));
        List<MiniGamePlayer> playersToAdd = new ArrayList<>();

        for (MiniGamePlayer player : getLobby().getPlayerList()) {
            if (player.isForceSpectator()) {
                player.setSpectator(true);
                player.getPlayer().teleport(config.getMap().getSpectatorStartLocation());
            } else {
                playersToAdd.add(player);
                player.applyQueuedKit();
            }
        }

        Logger.tryOrLog(() -> config.onPlayerAdd(playersToAdd));

        // do this after players have been added to map config; map config must be given players in bulk to handle properly
        for (MiniGamePlayer player : playersToAdd) {
            finishAddingPlayer(player);
            Helpers.setImmobile(player.getPlayer(), true);
        }

        new Countdown(MiniGamePlugin.getPlugin(), STARTING_COUNTDOWN_SECONDS, true, this, getLobby()) {
            @Override
            protected void onInterval() {
                for (MiniGamePlayer player : getLobby().getPlayerList())
                    Helpers.actionBarOrChat(player.getPlayer(), ChatColor.GRAY.toString() + "Starting in " + getRemainingIntervals() + "...");
            }

            @Override
            protected void onEnd() {
                for (MiniGamePlayer player : getLobby().getPlayerList())
                    Helpers.actionBarOrChat(player.getPlayer(), ChatColor.GREEN.toString() + ChatColor.BOLD +"START", 60);
                getLobby().setLobbyState(new InGameState(getLobby()));
            }
        };
    }

    @Override
    protected void switchOut(LobbyState to) {
        super.switchOut(to);
        for (MiniGamePlayer player : getLobby().getPlayerList()) {
            Helpers.setImmobile(player.getPlayer(), false);
        }
    }

    @Override
    protected void onPlayerQuit(MiniGamePlayer player) {
        super.onPlayerQuit(player);
        Helpers.setImmobile(player.getPlayer(), false);
    }
}