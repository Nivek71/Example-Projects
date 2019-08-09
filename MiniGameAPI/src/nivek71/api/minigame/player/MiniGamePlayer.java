package nivek71.api.minigame.player;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.MiniGameWinner;
import nivek71.api.minigame.events.MiniGamePlayerDeathEvent;
import nivek71.api.minigame.events.MiniGamePlayerKitChangeEvent;
import nivek71.api.minigame.events.MiniGamePlayerRespawnEvent;
import nivek71.api.minigame.events.MiniGamePlayerToggleForceSpectatorEvent;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.player.kit.KitManager;
import nivek71.api.minigame.player.kit.PlayerKit;
import nivek71.api.minigame.player.kit.PlayerKitType;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.Logger;
import nivek71.api.utility.containers.ImmutablePair;
import nivek71.api.utility.rule.RuleBound;
import nivek71.api.utility.rule.RuleBoundLinkBase;
import nivek71.api.utility.rule.rules.StandardRule;
import nivek71.api.utility.timer.Countdown;
import nivek71.api.utility.timer.Timer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;

public class MiniGamePlayer extends RuleBoundLinkBase implements MiniGameWinner {
    // MiniGamePlayer is always removed when player disconnects; as long as users remove MiniGamePlayer instances when
    // the player disconnects (like you would a Player instance) we won't have any invalid player objects floating around
    private final Player player;
    private final Lobby lobby;
    private boolean spectator, forceSpectator;
    private PlayerKit playerKit;
    private PlayerKitType<?> queuedKitType;
    private DamageProfile damageProfile = new DamageProfile();
    private Countdown reSpawnCountdown;
    private RuleBound parentBound, spectatorBounds;

    public MiniGamePlayer(Player player, Lobby lobby) {
        this.player = player;
        this.lobby = lobby;
    }

    public Player getPlayer() {
        return player;
    }

    public Lobby getLobby() {
        return lobby;
    }

    public MiniGame getMiniGame() {
        return lobby.getMiniGame();
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        if (this.spectator != spectator) {
            removeKit();
            Helpers.setInvisible(MiniGamePlugin.getPlugin(), player, spectator);
            player.setAllowFlight(spectator || player.getGameMode() == GameMode.CREATIVE);
            player.setFlying(spectator);
            this.spectator = spectator;
        }
    }

    @Override
    public boolean isParticipating() {
        return !isSpectator() || isReSpawning();
    }

    public boolean isForceSpectator() {
        return forceSpectator;
    }

    public void setForceSpectator(boolean forceSpectator) {
        if (this.forceSpectator != forceSpectator) {
            MiniGamePlayerToggleForceSpectatorEvent event = new MiniGamePlayerToggleForceSpectatorEvent(this, forceSpectator);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                this.forceSpectator = forceSpectator;
        }
    }

    public void killPlayer() {
        if (isParticipating()) {
            MiniGamePlayerDeathEvent deathEvent = new MiniGamePlayerDeathEvent(this, damageProfile.getMostRecentEntry(), getReSpawnDelay());
            Bukkit.getPluginManager().callEvent(deathEvent);
            if (!deathEvent.isCancelled()) {
                removeKit();
                if (deathEvent.getReSpawnDelay() == 0)
                    reSpawnNoCheck();
                else {
                    setSpectator(true);
                    player.setVelocity(player.getVelocity().add(player.getLocation().getDirection().multiply(-1.25)).add(new Vector(0, 3, 0)));
                    if (deathEvent.getReSpawnDelay() > 0) { // negative to disable
                        reSpawnCountdown = new Countdown(MiniGamePlugin.getPlugin(), deathEvent.getReSpawnDelay(), true, this, getLobby(), getLobby().getMiniGame()) {
                            @Override
                            protected void onInterval() {
                                Helpers.actionBarOrChat(player, ChatColor.GRAY + "Re-spawning in " + getRemainingIntervals() + "...");
                            }

                            @Override
                            protected void onEnd() {
                                Helpers.removeActionBar(player);
                                reSpawn();
                            }
                        };
                    } else getMiniGame().checkEnoughParticipants();
                }
            }
        }
    }

    public void spawn(Location spawnLocation) {
        Validate.isTrue(!isSpectator(), "Spectators cannot be spawned; use reSpawn instead");
        applyQueuedKit();
        player.teleport(spawnLocation);
    }

    private void reSpawnNoCheck() {
        MiniGamePlayerRespawnEvent event = new MiniGamePlayerRespawnEvent(this, getMiniGame().getMapConfiguration().getSpawnLocationFor(this));
        Bukkit.getPluginManager().callEvent(event);
        setSpectator(false);
        spawn(event.getSpawnLocation());
    }

    public void reSpawn() {
        Validate.isTrue(isParticipating(), "Only participants can reSpawn");
        if (isSpectator()) {
            cancelReSpawn();
            reSpawnNoCheck();
        }
    }

    public boolean isReSpawning() {
        return reSpawnCountdown != null;
    }

    public int getRemainingReSpawnSeconds() {
        return isReSpawning() ? reSpawnCountdown.getRemainingIntervals() : -1;
    }

    public void cancelReSpawn() {
        if (reSpawnCountdown != null) {
            reSpawnCountdown.cancel();
            reSpawnCountdown = null;
        }
    }

    public void removeKit() {
        Helpers.restorePlayer(player);
        player.getInventory().clear();
        if (playerKit != null) {
            Timer.cancelAssociated(playerKit);
            Logger.tryOrLog(() -> playerKit.removeFrom(this));
        }
    }

    public PlayerKit getKit() {
        return playerKit;
    }

    public PlayerKitType<?> getQueuedKitType() {
        return queuedKitType;
    }

    public void applyQueuedKit() {
        removeKit();
        this.playerKit = queuedKitType.getKitFactory().apply(this, Helpers.helper(queuedKitType));
        Logger.tryOrLog(() -> this.playerKit.giveTo(this));
    }

    public void setKitType(PlayerKitType<?> kitType) {
        MiniGamePlayerKitChangeEvent event = new MiniGamePlayerKitChangeEvent(this, this.playerKit, kitType, StandardRule.PLAYER_APPLY_KIT.getRuleState(this));
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            queuedKitType = event.getNextKitType();
            if (event.isChangeNow())
                applyQueuedKit();
            KitManager.setDefaultKit(getMiniGame().getInfo(), player.getUniqueId(), queuedKitType);
        }
    }

    public DamageProfile getDamageProfile() {
        return damageProfile;
    }

    public int getReSpawnDelay() {
        ImmutablePair<StandardRule.ReSpawnRule, Boolean> reSpawnRule = getRuleState(StandardRule.RESPAWN_IMMEDIATELY);
        if (reSpawnRule.getSecond() == null || !reSpawnRule.getSecond())
            return -1; // reSpawn disabled
        return reSpawnRule.getFirst().getReSpawnDelay();
    }

    private RuleBound get(RuleBound bound) {
        return bound == null ? lobby.getLobbyState() : bound;
    }

    @Override
    public RuleBound getParentBound() {
        return isSpectator() ? get(spectatorBounds) : get(parentBound);
    }

    public RuleBound getStandardBounds() {
        return parentBound;
    }

    public void setParentBounds(RuleBound parentBound) {
        this.parentBound = parentBound;
    }

    public RuleBound getSpectatorBounds() {
        return spectatorBounds;
    }

    public void setSpectatorBounds(RuleBound spectatorBounds) {
        this.spectatorBounds = spectatorBounds;
    }

    @Override
    public Collection<MiniGamePlayer> getPlayers() {
        return Collections.singleton(this);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }
}
