package nivek71.api.minigame;

import nivek71.api.minigame.events.EventHelper;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.lobby.rotations.MiniGameRotationCycle;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.Logger;
import nivek71.api.utility.containers.Pair;
import nivek71.api.utility.functions.RunnableEx;
import nivek71.api.utility.rule.RuleBound;
import nivek71.api.utility.rule.rules.Rule;
import nivek71.api.utility.rule.rules.StandardRule;
import nivek71.api.utility.timer.Timer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MiniGamePlugin extends JavaPlugin implements Listener {
    private static final Map<MiniGamePlayer, Party> parties = new HashMap<>();

    private static Location hubLocation;
    private static List<RunnableEx> enableFunctions = new ArrayList<>();
    // cannot use isEnabled() from JavaPlugin because JavaPlugin instance is not available when this is needed
    private static boolean enabled = false;

    static {
        // other classes may be loaded by static initializer as needed
        EventHelper.load();
    }

    public static MiniGamePlugin getPlugin() {
        return JavaPlugin.getPlugin(MiniGamePlugin.class);
    }

    public static Location getHubLocation() {
        return hubLocation;
    }

    public static void setHubLocation(Location hubLocation) {
        Validate.notNull(hubLocation, "hubLocation cannot be null");
        MiniGamePlugin.hubLocation = hubLocation;
    }

    public static void registerOnEnableFunction(RunnableEx runnableEx) {
        if (!enabled)
            enableFunctions.add(runnableEx);
        else Logger.tryOrLog(runnableEx);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        enabled = true;
        ConfigurationSection locationSection = getConfig().getConfigurationSection("hub-location");
        if (locationSection != null) {
            try {
                hubLocation = Location.deserialize(locationSection.getValues(false));
            } catch (IllegalArgumentException ex) { // will throw exception if invalid world
                Logger.logException(ex);
            }
        }
        // if locationSection is null, or world does not exist, make sure hubLocation is not null
        if (hubLocation == null)
            hubLocation = new Location(Bukkit.getWorlds().get(0), 0, 100, 0);

        for (RunnableEx runnableEx : enableFunctions)
            Logger.tryOrLog(runnableEx);
        enableFunctions.clear();

        Helpers.registerPlayerCleanup(player -> {
            MiniGamePlayer miniGamePlayer = Lobby.getMiniGamePlayer(player);
            if (miniGamePlayer != null) {
                removeFromParty(miniGamePlayer);
            }
        });

        // set default policies
        Rule.setBoundPoliciesFor(Helpers.CRAFT_PLAYER_CLASS, player -> {
            MiniGamePlayer miniGamePlayer = Lobby.getMiniGamePlayer(player);
            return miniGamePlayer == null ? RuleBound.EMPTY_BOUNDS : miniGamePlayer;
        });

        lobby = new Lobby(new Location(Bukkit.getWorlds().get(0), 100, 100, 100), new MiniGameRotationCycle());

        Bukkit.getPluginManager().registerEvents(this, this);

        Helpers.runLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                lobby.addPlayer(player);
            }
        });
    }

    @Override
    public void onDisable() {
        enabled = false;

        for (Player player : Bukkit.getOnlinePlayers())
            Lobby.removePlayer(player);
    }

    public static void registerParty(Set<MiniGamePlayer> players) {
        for (MiniGamePlayer player : players)
            parties.put(player, new Party(players));
    }

    public static Collection<MiniGamePlayer> getPartyOf(MiniGamePlayer player) {
        Party party = parties.get(player);
        return party == null ? null : party.players;
    }

    public static Pair<Collection<Collection<MiniGamePlayer>>, Collection<MiniGamePlayer>> getPartiesOf(Collection<? extends MiniGamePlayer> players) {
        Collection<MiniGamePlayer> soloPlayers = new ArrayList<>();
        // use Party class, instead of Set as key to speed up hashCode (Set's hashCode calls each elements hashCode)
        Map<Party, Collection<MiniGamePlayer>> partiesToLocalParties = new HashMap<>();

        for (MiniGamePlayer player : players) {
            Party party = parties.get(player);
            if (party == null)
                soloPlayers.add(player);
            else partiesToLocalParties.computeIfAbsent(party, k -> new ArrayList<>()).add(player);
        }
        return new Pair<>(partiesToLocalParties.values(), soloPlayers);
    }

    public static void removeFromParty(MiniGamePlayer player) {
        Party party = parties.remove(player);
        if (party != null)
            party.players.remove(player);
    }

    private static final class Party {
        private final Set<MiniGamePlayer> players;

        private Party(Set<MiniGamePlayer> players) {
            this.players = players;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Lobby.removePlayer(event.getPlayer()); // remove player from lobby, if player is in lobby
        Timer.cancelAssociated(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("kit")) {
            MiniGamePlayer player = Lobby.getMiniGamePlayer(sender);
            if (player == null || !StandardRule.PLAYER_QUEUE_KIT.getRuleState(player)) {
                sender.sendMessage(ChatColor.RED + "You cannot use this command.");
                return true;
            }

            player.getLobby().getMiniGame().getInfo().getKitManager().showKitSelectionInventoryTo(player.getPlayer());
        }
        return true;
    }

    // multiple lobbies may be configured, but for testing one makes much more sense
    // we'll automatically send player to lobby on join
    private Lobby lobby;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        lobby.addPlayer(event.getPlayer());
    }
}
