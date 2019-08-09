package nivek71.minigame.ctf;

import nivek71.api.minigame.MiniGame;
import nivek71.api.minigame.MiniGameInfo;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.ScoreboardBase;
import nivek71.api.utility.rule.rules.StandardRule;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CaptureTheFlag extends MiniGame<CTF_MapConfiguration> {
    private final CTF_Scoreboard ctfScoreboard = new CTF_Scoreboard();

    public CaptureTheFlag(Lobby lobby, MiniGameInfo<CaptureTheFlag, CTF_MapConfiguration> info, CTF_MapConfiguration configuration) {
        super(lobby, info, configuration);

        setRuleState(StandardRule.RESPAWN_AFTER(8), true);
        setRuleState(StandardRule.ENTITY_DAMAGE, true);
    }

    @Override
    public void onStart() {
        super.onStart();

        getLobby().getLobbyState().setRuleState(StandardRule.PLAYER_QUEUE_KIT, true);
    }

    @Override
    public ScoreboardBase getScoreboardFor(MiniGamePlayer player) {
        return ctfScoreboard;
    }

    public CTF_Scoreboard getCTFScoreboard() {
        return ctfScoreboard;
    }

    public final class CTF_Scoreboard extends ScoreboardBase {
        private static final int LINES_PER_TEAM = 4;
        private static final int TEAM_NAME = 1;
        private static final int FLAG_STATUS = 2;
        private static final int TEAM_POINTS = 3;
        private final List<CTF_Team> teams = new ArrayList<>(getMapConfiguration().getTeams());

        // Team <name>
        //  Flag: Safe/Stolen
        //  Points: <points>

        @Override
        public String getTitle(Player player) {
            return ChatColor.BLUE.toString() + ChatColor.BOLD + "CTF";
        }

        public void updateFlagStatusLine(CTF_Team team) {
            updateTeamLine(team, FLAG_STATUS);
        }

        public void updatePointsLine(CTF_Team team) {
            updateTeamLine(team, TEAM_POINTS);
        }

        public void updateTeamLine(CTF_Team team, int index) {
            updateLine(teams.indexOf(team) * LINES_PER_TEAM + index);
        }

        @Override
        public String getLine(Player player, int i) {
            int subIndex = i % LINES_PER_TEAM;
            if (subIndex == 0)
                return "";
            int teamIndex = i / LINES_PER_TEAM;
            Validate.validIndex(teams, teamIndex, "line out of bounds");

            CTF_Team team = teams.get(teamIndex);
            switch (subIndex) {
                // will never be 0 (first line checked)
                case 1:
                    return team.getColor() + "Team " + team.getName();
                case 2:
                    return ChatColor.GRAY + " Flag: " + (team.isFlagStolen() ? ChatColor.RED + "Stolen" : ChatColor.GRAY + "Safe");
                case 3:
                    return ChatColor.GRAY + " Points: " + ChatColor.WHITE + team.getTeamPoints();
            }
            throw new RuntimeException("logic error (should never be able to get this far)");
        }

        @Override
        public int getLineCount(Player player) {
            return getMapConfiguration().getTeams().size() * 4;
        }
    }

}
