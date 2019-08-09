package nivek71.minigame.ctf;

import nivek71.api.minigame.map.MiniGameMap;
import nivek71.api.minigame.map.configurations.MapTeamConfiguration;
import nivek71.api.minigame.map.configurations.teamdivider.TeamDivider;

public class CTF_MapConfiguration extends MapTeamConfiguration<CTF_MapConfiguration, CaptureTheFlag, CTF_Team> {
    private int pointsToWin;

    public CTF_MapConfiguration(MiniGameMap map, boolean allowMapDamage, TeamDivider<CTF_Team> teamDivider, int pointsToWin) {
        super(CaptureTheFlag.class, map, allowMapDamage, teamDivider);
        this.pointsToWin = pointsToWin;
    }

    public CTF_MapConfiguration(MiniGameMap map, boolean allowMapDamage, int pointsToWin, CTF_Team... teams) {
        super(CaptureTheFlag.class, map, allowMapDamage, teams);
        this.pointsToWin = pointsToWin;
    }

    public int getPointsToWin() {
        return pointsToWin;
    }

    public void setPointsToWin(int pointsToWin) {
        this.pointsToWin = pointsToWin;
    }
}
