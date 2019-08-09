package nivek71.api.minigame;

import nivek71.api.minigame.player.MiniGamePlayer;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.Collections;

public interface MiniGameWinner {
    MiniGameWinner NOBODY = new MiniGameWinner() {
        @Override
        public Collection<MiniGamePlayer> getPlayers() {
            return Collections.emptyList();
        }

        @Override
        public String getName() {
            return "Nobody";
        }

        @Override
        public ChatColor getColor() {
            return ChatColor.GRAY;
        }

        @Override
        public boolean isParticipating() {
            return false;
        }
    };

    Collection<MiniGamePlayer> getPlayers();

    String getName();
    ChatColor getColor();
    boolean isParticipating();
}
