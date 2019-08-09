package nivek71.api.minigame.lobby.lobbystates;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.events.MapMadeAvailableEvent;
import nivek71.api.minigame.events.MiniGamePlayerKitChangeEvent;
import nivek71.api.minigame.events.MiniGamePlayerToggleForceSpectatorEvent;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.lobby.LobbyState;
import nivek71.api.minigame.lobby.lobbystates.supports.SupportChangeMiniGame;
import nivek71.api.minigame.lobby.rotations.MiniGameRotationCycle;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.minigame.player.kit.KitManager;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.Logger;
import nivek71.api.utility.ScoreboardBase;
import nivek71.api.utility.rule.rules.StandardRule;
import nivek71.api.utility.timer.Countdown;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

@SupportChangeMiniGame
public class WaitingState extends LobbyState {
    private Countdown countdown;
    private StateScoreboard stateScoreboard;
    private int activePlayers = 0;
    private boolean initialized = false;
    public static final int MIN_PLAYER_START_SECONDS = 60, MAX_PLAYER_START_SECONDS = 15;
    private Listener waitingMapListener;

    public WaitingState(Lobby lobby) {
        super(lobby);
        setRuleState(StandardRule.PLAYER_QUEUE_KIT, true);
    }

    public StateScoreboard getStateScoreboard() {
        return stateScoreboard;
    }

    private int getActivePlayerCount() {
        return activePlayers;
    }

    private void setActivePlayers(int activePlayers) {
        this.activePlayers = activePlayers;
        stateScoreboard.updateLine(StateScoreboard.PLAYERS_LINE);
    }

    private void attemptCountdownStart() {
        int activePlayerCount = getActivePlayerCount();
        if (waitingMapListener == null && countdown == null && activePlayerCount >= getLobby().getMiniGame().getInfo().getMinPlayersToStart()) {
            countdown = new Countdown(MiniGamePlugin.getPlugin(), MIN_PLAYER_START_SECONDS, true, this, getLobby()) {
                @Override
                protected void onInterval() {
                    stateScoreboard.updateLine(StateScoreboard.STATUS_LINE);
                }

                @Override
                protected void onEnd() {
                    getLobby().setLobbyState(new StartingState(getLobby()));
                }
            };
        } else if (activePlayerCount >= getLobby().getMaxSize() && countdown.getRemainingIntervals() > 15) {
            countdown.setRemainingIntervals(MAX_PLAYER_START_SECONDS);
        }
    }

    private MiniGame<?> getNextMiniGame(int attemptCount) {
        switch (attemptCount) {
            case 0: // initial try
            case 1: // try again
                break;
            case 2:
                // change rotation to known state
                getLobby().setMiniGameRotation(new MiniGameRotationCycle(getLobby().getMiniGameRotation().getAvailableMiniGames()));
                break;
            default:
                throw new IllegalStateException("Failed to switch MiniGame; MiniGameRotation failed");
        }
        return Logger.fetchOrLog(() -> getLobby().getMiniGameRotation().getNextMiniGame(getLobby()), () -> getNextMiniGame(attemptCount + 1));
    }

    private void addPlayer(MiniGamePlayer player) {
        stateScoreboard.setScoreboardFor(player.getPlayer());
        player.getPlayer().teleport(getLobby().getLobbyLocation());
        if (!player.isForceSpectator())
            setActivePlayers(activePlayers + 1);
    }

    private void initialize() {
        if (initialized)
            return;
        initialized = true;
        MiniGame<?> miniGame = getNextMiniGame(0);
        if (miniGame == null)
            waitingMapListener = Helpers.registerDynamicEvent(MiniGamePlugin.getPlugin(), MapMadeAvailableEvent.class, event -> {
                if (!event.isStillAvailable())
                    return;

                MiniGame<?> next = getLobby().getMiniGameRotation().getNextMiniGame(getLobby(), event.getMap());
                if (next != null) {
                    getLobby().setMiniGame(next);
                    HandlerList.unregisterAll(waitingMapListener);
                    waitingMapListener = null;
                    attemptCountdownStart();
                    stateScoreboard.updateLines();
                }
            }, EventPriority.HIGH, false);
        else getLobby().setMiniGame(miniGame);
        stateScoreboard = new StateScoreboard();
    }

    @Override
    protected void switchIn(LobbyState previous) {
        super.switchIn(previous);
        if (getLobby().getPlayerList().size() > 0) {
            initialize();
            getLobby().getPlayerList().forEach(this::addPlayer);
            attemptCountdownStart();
        }
    }

    @Override
    protected void switchOut(LobbyState to) {
        super.switchOut(to);
        for (MiniGamePlayer player : getLobby().getPlayerList())
            ScoreboardBase.removeScoreboard(player.getPlayer(), stateScoreboard);
    }

    @Override
    protected void onPlayerJoin(MiniGamePlayer player) {
        super.onPlayerJoin(player);
        initialize();
        player.setKitType(KitManager.getDefaultKit(getLobby().getMiniGame().getInfo(), player.getPlayer().getUniqueId()));
        addPlayer(player);
        attemptCountdownStart();
        stateScoreboard.updateLine(StateScoreboard.STATUS_LINE);
    }

    @Override
    protected void onPlayerQuit(MiniGamePlayer player) {
        super.onPlayerQuit(player);
        if (!player.isForceSpectator())
            setActivePlayers(activePlayers - 1);
        if (countdown != null && getActivePlayerCount() < getLobby().getMiniGame().getInfo().getMinPlayersToStart()) {
            countdown.cancel(); // no longer enough players to start
            countdown = null;
        }
        ScoreboardBase.removeScoreboard(player.getPlayer(), stateScoreboard);
        stateScoreboard.updateLine(StateScoreboard.STATUS_LINE);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKitUpdate(MiniGamePlayerKitChangeEvent event) {
        // if the player is not in this lobby, update will silently fail
        // run in one tick (event is called before kit is properly changed; calling scoreboard to update too early will
        // have the scoreboard keep the soon to be changed kit).
        Helpers.runLater(MiniGamePlugin.getPlugin(), () -> stateScoreboard.updateLine(event.getPlayer(), StateScoreboard.KIT_LINE));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onForceSpectatorChange(MiniGamePlayerToggleForceSpectatorEvent event) {
        if (event.getLobby().equals(getLobby())) {
            if (event.getNextState())
                setActivePlayers(activePlayers - 1);
            else setActivePlayers(activePlayers + 1);
        }
    }

    public class StateScoreboard extends ScoreboardBase {
        public static final int LINE_COUNT = 15;
        public static final int PLAYERS_LINE = 2;
        public static final int NEXT_GAME_LINE = 5;
        public static final int MAP_LINE = 8;
        public static final int KIT_LINE = 11;
        public static final int STATUS_LINE = 14;

        @Override
        public String getTitle(Player player) {
            return ChatColor.GOLD.toString() + ChatColor.BOLD + "Welcome, " + player.getName();
        }

        @Override
        public String getLine(Player player, int i) {
            MiniGamePlayer miniGamePlayer = Lobby.getMiniGamePlayer(player);
            MiniGame miniGame = getLobby().getMiniGame();
            switch (i) {
                case 0:
                case 3:
                case 6:
                case 9:
                case 12:
                    return "";
                case 1:
                    return ChatColor.BLUE.toString() + ChatColor.BOLD + "PLAYERS";
                case 2:
                    return " " + activePlayers + "/" + getLobby().getMaxSize();
                case 4:
                    return ChatColor.GREEN.toString() + ChatColor.BOLD + "NEXT GAME";
                case 5:
                    return " " + (miniGame == null ? "None" : miniGame.getInfo().getName());
                case 7:
                    return ChatColor.RED.toString() + ChatColor.BOLD + "MAP";
                case 8:
                    return " " + (miniGame == null ? "None" : miniGame.getMapConfiguration().getMap().getName());
                case 10:
                    return ChatColor.AQUA.toString() + ChatColor.BOLD + "KIT";
                case 11:
                    return " " + (miniGamePlayer.getQueuedKitType() == null ? "None" : miniGamePlayer.getQueuedKitType().getName());
                case 13:
                    return ChatColor.GRAY.toString() + ChatColor.BOLD + "STATUS";
                case 14:
                    return " " + (waitingMapListener != null ? "Waiting for an available map" : (countdown == null ? "Waiting for players..." : "Starting in " + countdown.getRemainingIntervals()));
            }
            throw new IllegalArgumentException("Invalid line: " + i + " (max is " + LINE_COUNT + ").");
        }

        @Override
        public int getLineCount(Player player) {
            return LINE_COUNT;
        }
    }
}
