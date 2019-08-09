package nivek71.api.minigame.lobby.lobbystates;

import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.lobby.LobbyState;
import nivek71.api.minigame.map.MiniGameMap;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.timer.Countdown;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class EndingState extends ActiveMiniGameStateBase {
    public static final int ENDING_LENGTH = 15;
    private static final int FIREWORKS_PER_SECOND = 3;
    private static final int FIREWORK_MAX_Y_OFFSET = 20;

    public EndingState(Lobby lobby) {
        super(lobby);
    }

    @Override
    protected void switchIn(LobbyState previous) {
        super.switchIn(previous);

        new Countdown(MiniGamePlugin.getPlugin(), ENDING_LENGTH, true, this, getLobby()) {

            MiniGameMap map = getLobby().getMiniGame().getMapConfiguration().getMap();
            final int minX, maxX;
            final int minY, maxY;
            final int minZ, maxZ;

            {
                Location lowerCorner = map.getLowestCorner(), highestCorner = map.getHighestCorner();
                minX = lowerCorner.getBlockX();
                maxX = highestCorner.getBlockX();
                maxY = map.getFireworkLaunchY() - (FIREWORK_MAX_Y_OFFSET / 2);
                minY = maxY + FIREWORK_MAX_Y_OFFSET;
                minZ = lowerCorner.getBlockZ();
                maxZ = highestCorner.getBlockZ();
            }

            @Override
            protected void onInterval() {
                for (int i = 0; i < FIREWORKS_PER_SECOND; i++) {
                    Location fireworkLocation = new Location(map.getCorner1().getWorld(),
                            Helpers.random(minX, maxX), Helpers.random(minY, maxY),
                            Helpers.random(minZ, maxZ));
                    ChatColor[] values = ChatColor.values();
                    Helpers.launchFirework(fireworkLocation, values[Helpers.random(0, values.length)], values[Helpers.random(0, values.length)]);
                }
            }

            @Override
            protected void onEnd() {
                getLobby().setLobbyState(new WaitingState(getLobby()));
            }
        };
    }

    @Override
    protected void switchOut(LobbyState to) {
        super.switchOut(to);
        Helpers.runLater(MiniGamePlugin.getPlugin(), () -> getLobby().getMiniGame().getMapConfiguration().getMap().releaseMap(getLobby()), 20);
    }
}
