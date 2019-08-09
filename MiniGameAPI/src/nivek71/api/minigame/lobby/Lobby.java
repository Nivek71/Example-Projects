package nivek71.api.minigame.lobby;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.events.LobbyChangeMiniGameEvent;
import nivek71.api.minigame.events.MiniGamePlayerLeaveLobbyEvent;
import nivek71.api.minigame.lobby.lobbystates.WaitingState;
import nivek71.api.minigame.lobby.lobbystates.supports.SupportChangeMiniGame;
import nivek71.api.minigame.lobby.rotations.MiniGameRotation;
import nivek71.api.minigame.map.MiniGameMap;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.minigame.player.kit.KitManager;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.Logger;
import nivek71.api.utility.rule.RuleBoundBase;
import nivek71.api.utility.timer.Timer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lobby extends RuleBoundBase {
    private static final Map<Player, MiniGamePlayer> playerMap = new HashMap<>();
    public static final int DEFAULT_LOBBY_MAX_SIZE = 16;
    private LobbyState lobbyState;
    private Location lobbyLocation;
    private List<MiniGamePlayer> playerList = new ArrayList<>();
    private MiniGame miniGame;
    private MiniGameRotation miniGameRotation;

    public Lobby(Location lobbyLocation, MiniGameRotation miniGameRotation) {
        this.lobbyLocation = lobbyLocation;
        this.miniGameRotation = miniGameRotation;
        setLobbyState(new WaitingState(this));
    }

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public void setLobbyState(LobbyState lobbyState) {
        Validate.notNull(lobbyState, "lobbyState cannot be null");
        if (this.lobbyState != null) {
            Logger.tryOrLog(() -> this.lobbyState.switchOut(lobbyState));
            Timer.cancelAssociated(this.lobbyState);
        }
        if (MiniGamePlugin.getPlugin().isEnabled()) { // if plugin is being disabled, do not switch in next state
            // use temp so that field reassignment occurs before switchIn (in case switchIn references lobbyState through getter)
            LobbyState previous = this.lobbyState;
            this.lobbyState = lobbyState;
            Logger.tryOrLog(() -> this.lobbyState.switchIn(previous));
        }
    }

    public void addPlayer(Player player) {
        Validate.notNull(player, "player cannot be null");
        Helpers.restorePlayer(player);
        MiniGamePlayer miniGamePlayer = new MiniGamePlayer(player, this);
        playerMap.put(player, miniGamePlayer);
        playerList.add(miniGamePlayer);
        Logger.tryOrLog(() -> lobbyState.onPlayerJoin(miniGamePlayer));
    }

    public static void removePlayer(Player player) {
        Validate.notNull(player, "player cannot be null");
        // onPlayerQuit should be called before the player is removed from lobbyMap; get then remove
        MiniGamePlayer miniGamePlayer = playerMap.get(player);
        // if miniGamePlayer is null then player is not in a lobby
        if (miniGamePlayer != null) {
            Bukkit.getPluginManager().callEvent(new MiniGamePlayerLeaveLobbyEvent(miniGamePlayer));

            Logger.tryOrLog(() -> miniGamePlayer.getLobby().lobbyState.onPlayerQuit(miniGamePlayer));
            MiniGamePlugin.removeFromParty(miniGamePlayer);
            playerMap.remove(player);
            miniGamePlayer.getLobby().playerList.remove(miniGamePlayer);
            Timer.cancelAssociated(miniGamePlayer);
            player.teleport(MiniGamePlugin.getHubLocation());
            Helpers.restorePlayer(player);
        }
    }

    public static MiniGamePlayer getMiniGamePlayer(CommandSender sender) {
        //noinspection SuspiciousMethodCalls
        return playerMap.get(sender);
    }

    public static Lobby getPlayersLobby(Player player) {
        MiniGamePlayer miniGamePlayer = getMiniGamePlayer(player);
        return miniGamePlayer == null ? null : miniGamePlayer.getLobby();
    }

    public MiniGame<?> getMiniGame() {
        return miniGame;
    }

    public void setMiniGame(MiniGame miniGame) {
        Validate.notNull(miniGame, "miniGame cannot be null");
        Validate.isTrue(lobbyState.supports(SupportChangeMiniGame.class),
                "Cannot change Lobby MiniGame; current lobbyState does is not annotated by SupportChangeMiniGame");
        MiniGameMap map = miniGame.getMapConfiguration().getMap();
        Validate.isTrue(map.isAvailable(), "Cannot set MiniGame; selected MiniGame's map is not available");
        map.claimMap(miniGame);
        Bukkit.getPluginManager().callEvent(new LobbyChangeMiniGameEvent(this.miniGame, miniGame));
        this.miniGame = miniGame;
        for (MiniGamePlayer player : getPlayerList())
            player.setKitType(KitManager.getDefaultKit(miniGame.getInfo(), player.getPlayer().getUniqueId()));
    }

    public MiniGameRotation getMiniGameRotation() {
        return miniGameRotation;
    }

    public void setMiniGameRotation(MiniGameRotation miniGameRotation) {
        Validate.notNull(miniGameRotation, "miniGameRotation cannot be null");
        this.miniGameRotation = miniGameRotation;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        Validate.notNull(lobbyLocation, "lobbyLocation cannot be null");
        this.lobbyLocation = lobbyLocation;
    }

    public List<MiniGamePlayer> getPlayerList() {
        return playerList;
    }

    // not enforced
    public int getMaxSize() {
        return DEFAULT_LOBBY_MAX_SIZE; // in future, may make sense to make this dynamic; use getter just in case
    }

    public void broadcast(String... messages) {
        for (MiniGamePlayer miniGamePlayer : playerList) {
            miniGamePlayer.getPlayer().sendMessage(messages);
        }
    }

    public Collection<? extends MiniGamePlayer> getNonSpectators() {
        List<MiniGamePlayer> players = new ArrayList<>();
        playerList.forEach(player -> {
            if (player.isSpectator())
                players.add(player);
        });
        return players;
    }
}
