package nivek71.api.utility;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ScoreboardBase {
    private static final Map<Player, ScoreboardBase> playerToScoreboardMap = new HashMap<>();
    private static final Scoreboard EMPTY_SCOREBOARD = Bukkit.getScoreboardManager().getNewScoreboard();
    private Set<Player> playersWithScoreboard = new HashSet<>();

    static {
        Helpers.registerPlayerCleanup(ScoreboardBase::removeScoreboard);
    }

    public static ScoreboardBase getScoreboardOf(Player player) {
        return playerToScoreboardMap.get(player);
    }

    public static boolean removeScoreboard(Player player, ScoreboardBase scoreboard) {
        if (playerToScoreboardMap.remove(player, scoreboard)) {
            scoreboard.removeScoreboardFrom(player);
            return true;
        }
        return false;
    }

    public static ScoreboardBase removeScoreboard(Player player) {
        ScoreboardBase scoreboard = playerToScoreboardMap.remove(player);
        if (scoreboard != null) {
            scoreboard.removeScoreboardFrom(player);
        }
        return scoreboard;
    }

    protected void removeScoreboardFrom(Player player) {
        // should be removed from map before calling
        playersWithScoreboard.remove(player);
        player.setScoreboard(EMPTY_SCOREBOARD);
    }


    public abstract String getTitle(Player player);
    public abstract String getLine(Player player, int i);
    public abstract int getLineCount(Player player);

    protected Scoreboard getScoreboardFor(Player player) {
        return createScoreboard(player);
    }

    private void updateTitleNoCheck(Player player) {
        player.getScoreboard().getObjective("side").setDisplayName(getTitle(player));
    }

    public boolean updateTitle(Player player) {
        if (playersWithScoreboard.contains(player)) {
            updateTitleNoCheck(player);
            return true;
        }
        return false;
    }

    public void updateTitle() {
        for (Player player : playersWithScoreboard) {
            updateTitleNoCheck(player);
        }
    }

    private void updateLineNoCheck(Player player, int i) {
        Team team = player.getScoreboard().getTeam(String.valueOf(i));
        Validate.notNull(team, "line out of bounds: " + i); // ok, maybe just ONE check
        team.setPrefix(getLine(player, i));
    }

    public boolean updateLine(Player player, int i) {
        if (playersWithScoreboard.contains(player)) {
            updateLineNoCheck(player, i);
            return true;
        }
        return false;
    }

    public void updateLine(int i) {
        for (Player player : playersWithScoreboard) {
            updateLineNoCheck(player, i);
        }
    }

    private void updateLinesNoCheck(Player player) {
        int length = getLineCount(player);
        for (int i = 0; i < length; i++)
            updateLineNoCheck(player, i);
    }

    public boolean updateLines(Player player) {
        if (playersWithScoreboard.contains(player)) {
            updateLinesNoCheck(player);
            return true;
        }
        return false;
    }

    public void updateLines() {
        for (Player player : playersWithScoreboard) {
            updateLinesNoCheck(player);
        }
    }

    protected Scoreboard createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("side", "dummy", getTitle(player));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        int lines = getLineCount(player);
        for (int i = 0; i < lines; i++) {
            Team team = scoreboard.registerNewTeam(String.valueOf(i));
            String entry = ChatColor.values()[i].toString();
            team.addEntry(entry);
            team.setPrefix(getLine(player, i));
            objective.getScore(entry).setScore(lines - i);
        }
        return scoreboard;
    }

    public void setScoreboardFor(Player player) {
        removeScoreboard(player);
        player.setScoreboard(getScoreboardFor(player));
        playerToScoreboardMap.put(player, this);
        playersWithScoreboard.add(player);
    }
}
