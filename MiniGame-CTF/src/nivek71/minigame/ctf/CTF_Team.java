package nivek71.minigame.ctf;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.events.MiniGamePlayerDeathEvent;
import nivek71.api.minigame.events.MiniGamePlayerLeaveLobbyEvent;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.map.MiniGameTeam;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.area.Area;
import nivek71.api.utility.area.BoxedArea;
import nivek71.api.utility.timer.Repeater;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class CTF_Team extends MiniGameTeam<CTF_MapConfiguration> {
    public static final int DEFAULT_FLAG_CAPTURE_HALF_LENGTH = 5;
    public static final int FLAG_UPDATE_PERIOD = 4;
    public static final int PARTICLE_COUNT_ON_FLAG_RESET = 10;
    public static final float PARTICLE_ON_FLAG_RESET_OFFSET = 0.3f;
    private Material previousFlagMaterial = Material.AIR;
    private int teamPoints = 0;
    private Location flagLocation;
    private Area captureArea;
    private MiniGamePlayer flagCarrier;
    private Repeater flagHolderRepeater;
    private Listener listener;

    public CTF_Team(String name, ChatColor teamColor, Location flagLocation, Area captureArea, Location... spawnLocations) {
        super(name, teamColor, spawnLocations);
        Validate.notNull(flagLocation, "flagLocation cannot be null");
        Validate.notNull(captureArea, "captureArea cannot be null");
        this.flagLocation = flagLocation;
        this.captureArea = captureArea;
    }

    public CTF_Team(String name, ChatColor teamColor, Location flagLocation, Location... spawnLocations) {
        this(name, teamColor, flagLocation, new BoxedArea(
                new Location(flagLocation.getWorld(), flagLocation.getX() - DEFAULT_FLAG_CAPTURE_HALF_LENGTH,
                        flagLocation.getY() - DEFAULT_FLAG_CAPTURE_HALF_LENGTH, flagLocation.getZ() - DEFAULT_FLAG_CAPTURE_HALF_LENGTH),
                new Location(flagLocation.getWorld(), flagLocation.getX() + DEFAULT_FLAG_CAPTURE_HALF_LENGTH,
                        flagLocation.getY() + DEFAULT_FLAG_CAPTURE_HALF_LENGTH, flagLocation.getZ() + DEFAULT_FLAG_CAPTURE_HALF_LENGTH)
        ), spawnLocations);
    }

    public Location getFlagLocation() {
        return flagLocation;
    }

    public void setFlagLocation(Location flagLocation) {
        getFlagLocation().getBlock().setType(previousFlagMaterial);
        previousFlagMaterial = this.flagLocation.getBlock().getType();
        this.flagLocation = flagLocation;
        resetFlag();
    }

    public Area getCaptureArea() {
        return captureArea;
    }

    public void setCaptureArea(Area captureArea) {
        this.captureArea = captureArea;
    }

    public int getTeamPoints() {
        return teamPoints;
    }

    public void setTeamPoints(int teamPoints) {
        this.teamPoints = teamPoints;
        getMapConfiguration().getMiniGame().getCTFScoreboard().updatePointsLine(this);
        if (this.teamPoints >= getMapConfiguration().getPointsToWin()) {
            getMapConfiguration().getMiniGame().endGame(this);
        }
    }

    public Material getFlagMaterial() {
        return Helpers.chatColorToWoolColor(getColor());
    }

    public MiniGamePlayer getFlagCarrier() {
        return flagCarrier;
    }

    public boolean isFlagStolen() {
        return flagCarrier != null;
    }

    private void stopFlagCarrier() {
        if (this.flagCarrier != null) {
            flagHolderRepeater.cancel();
            flagHolderRepeater = null;
            this.flagCarrier = null;
            getMapConfiguration().getMiniGame().getCTFScoreboard().updateFlagStatusLine(this);
        }
    }

    private void stealFlag(MiniGamePlayer player) {
        Validate.notNull(player, "player cannot be null");
        Validate.isTrue(!player.isSpectator(), "player must not be spectator");
        stopFlagCarrier();
        flagLocation.getBlock().setType(Material.AIR);
        this.flagCarrier = player;

        CTF_Team flagCarrierTeam = getMapConfiguration().getPlayersTeam(flagCarrier);
        getMapConfiguration().getActiveLobby().broadcast(flagCarrierTeam.getColor() +
                flagCarrier.getPlayer().getName() + " stole " + getColor() + "Team " + getName() +
                flagCarrierTeam.getColor() + "'s flag.");

        getMapConfiguration().getMiniGame().getCTFScoreboard().updateFlagStatusLine(this);

        flagHolderRepeater = new Repeater(CTF_Plugin.getPlugin(), FLAG_UPDATE_PERIOD, true, this) {
            final int fireworkAmount = 60 / FLAG_UPDATE_PERIOD; // three seconds
            final int fireworkPower = 2;
            int amount;

            @Override
            public void onRepeat() {
                CTF_Team flagCarrierTeam = getMapConfiguration().getPlayersTeam(flagCarrier);
                if (flagCarrierTeam.captureArea.isInside(flagCarrier.getPlayer())) {
                    getMapConfiguration().getActiveLobby().broadcast(flagCarrierTeam.getColor() +
                            flagCarrier.getPlayer().getName() + " captured " + getColor() + "Team " + getName() +
                            flagCarrierTeam.getColor() + "'s flag.");
                    flagCarrierTeam.setTeamPoints(flagCarrierTeam.teamPoints + 1);
                    resetFlag();
                } else if (++amount % fireworkAmount == 0)
                    Helpers.launchFirework(flagCarrier.getPlayer().getLocation(), meta -> {
                        meta.setPower(fireworkPower);
                        meta.addEffect(Helpers.getFireworkEffect(getColor(), flagCarrierTeam.getColor()));
                    });
            }
        };
    }

    public void setFlagCarrier(MiniGamePlayer flagCarrier) {
        if (this.flagCarrier == flagCarrier)
            return;
        if (this.flagCarrier == null) // if the flag carrier was null, and is no longer null, same as stealing flag
            stealFlag(flagCarrier);
        else if (flagCarrier == null) // if flag carrier is being set to null, and it was not previously null, same as stopping flag carrier
            resetFlag();
        else this.flagCarrier = flagCarrier; // otherwise simply transfer flag ownership
    }

    public void resetFlag() {
        stopFlagCarrier();
        Block flagBlock = flagLocation.getBlock();
        Material flagMaterial = getFlagMaterial();
        if (flagBlock.getType() != flagMaterial) {
            flagBlock.setType(flagMaterial, false);
            flagLocation.getWorld().spawnParticle(Particle.FLAME, flagLocation, PARTICLE_COUNT_ON_FLAG_RESET, PARTICLE_COUNT_ON_FLAG_RESET,
                    PARTICLE_ON_FLAG_RESET_OFFSET, PARTICLE_ON_FLAG_RESET_OFFSET);
        }
    }

    @Override
    public void prepareTeam(CTF_MapConfiguration configuration) {
        super.prepareTeam(configuration);
        previousFlagMaterial = getFlagLocation().getBlock().getType();
        resetFlag();
    }

    private void stopFlagCarrier(MiniGamePlayer player) {
        if (player.equals(flagCarrier)) {
            CTF_Team enemyTeam = getMapConfiguration().getPlayersTeam(flagCarrier);
            player.getLobby().broadcast(enemyTeam.getColor() + flagCarrier.getName() + " dropped " + getColor() + "Team " +
                    getName() + enemyTeam.getColor() + "'s flag.");
            resetFlag();
        }
    }

    @Override
    public void startTeam(CTF_MapConfiguration mapConfiguration) {
        super.startTeam(mapConfiguration);
        listener = Helpers.registerDynamicEvent(CTF_Plugin.getPlugin(), BlockBreakEvent.class, event -> {
            if (event.getBlock().getLocation().equals(flagLocation)) {
                MiniGamePlayer player = Lobby.getMiniGamePlayer(event.getPlayer());
                if (player == null || player.getLobby() != getMapConfiguration().getActiveLobby() || player.isSpectator()) {
                    event.setCancelled(true);
                } else if (isOnTeam(player)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You cannot break your own team flag.");
                    event.setCancelled(true);
                } else {
                    if (!isFlagStolen())
                        stealFlag(player);
                }
            }
        });
        Helpers.registerDynamicEvent(CTF_Plugin.getPlugin(), listener, MiniGamePlayerDeathEvent.class, event -> stopFlagCarrier(event.getMiniGamePlayer()), EventPriority.MONITOR, true);
        Helpers.registerDynamicEvent(CTF_Plugin.getPlugin(), listener, MiniGamePlayerLeaveLobbyEvent.class, event -> stopFlagCarrier(event.getMiniGamePlayer()), EventPriority.MONITOR, true);
    }

    @Override
    public void onMiniGameEnd(MiniGame<CTF_MapConfiguration> miniGame) {
        super.onMiniGameEnd(miniGame);
        HandlerList.unregisterAll(listener);
    }

    @Override
    public void onMapReset() {
        super.onMapReset();
        stopFlagCarrier();
        teamPoints = 0;
        getFlagLocation().getBlock().setType(previousFlagMaterial);
        previousFlagMaterial = null;
    }
}
