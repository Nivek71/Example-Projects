package nivek71.api.minigame.map.configurations.teamdivider;

import nivek71.api.minigame.map.MiniGameTeam;
import nivek71.api.minigame.player.MiniGamePlayer;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class WeighedTeamDivider<T extends MiniGameTeam<?>> implements TeamDivider<T> {
    public static final int DEFAULT_PRIORITY = 100;
    private int totalPriority = 0;
    private List<TeamPriority> priorities = new ArrayList<>();

    public void registerTeam(T team, int priority) {
        Validate.notNull(team, "team cannot be null");
        Validate.isTrue(priority > 0, "priority must be greater than 0");
        priorities.add(new TeamPriority(team, priority));
        totalPriority+=priority;
    }

    @Override
    public void registerTeam(T team) {
        registerTeam(team, DEFAULT_PRIORITY);
    }

    @Override
    public void dividePlayers(Collection<Collection<MiniGamePlayer>> playerGroups, Collection<MiniGamePlayer> soloPlayers) {
        Validate.notEmpty(priorities, "cannot divide along 0 teams");
        // players will shift to registered
        final int totalPlayers = getRegisteredTeamsPlayerCount() + TeamDivider.getPlayerAmount(playerGroups, soloPlayers);
        // teams with the highest slots available are pushed to the front of the queue
        Comparator<TeamPriority> teamPriorityComparator = Comparator.comparingInt((teamPriority) -> teamPriority.getMaxTeamSize(totalPlayers));
        PriorityQueue<TeamPriority> recruitingTeamQueue = new PriorityQueue<>(teamPriorityComparator.reversed());
        recruitingTeamQueue.addAll(priorities);

        // groups with the most amount of players are pushed to the front of the queue
        Comparator<Collection<MiniGamePlayer>> orderedGroupComparator = Comparator.comparingInt(Collection::size);
        PriorityQueue<Collection<MiniGamePlayer>> playerGroupsQueue = new PriorityQueue<>(orderedGroupComparator.reversed());
        playerGroupsQueue.addAll(playerGroups);

        while (!playerGroupsQueue.isEmpty()) {
            List<MiniGamePlayer> largestGroup = new ArrayList<>(playerGroupsQueue.remove());
            TeamPriority largestSlotTeam = recruitingTeamQueue.remove();

            // the group is split into two groups (the second may be empty)
            // the largest group of players are added to the largest empty team
            // if the entire group could not fit into the team, the remaining players are put put in a second group, and
            // sent through again. If the team has remaining slots after adding this group, the team is re-added to the
            // queue of recruiting teams
            int splitGroupOneSize = Math.min(largestSlotTeam.getExtraSlotsAvailable(totalPlayers), largestGroup.size());

            if (splitGroupOneSize != largestGroup.size()) {
                // if not all players can fit, randomize who gets in
                Collections.shuffle(largestGroup);
                // add second group of players back into the collection (these players did not make it with the rest)
                playerGroupsQueue.add(new ArrayList<>(largestGroup.subList(splitGroupOneSize, largestGroup.size())));
            } else if (largestSlotTeam.getExtraSlotsAvailable(totalPlayers) != 0) // if this team can fit more players, put it back into the cycle
                recruitingTeamQueue.add(largestSlotTeam);
            // add the main bulk of the group to the largest available team
            largestSlotTeam.getTeam().addPlayers(largestGroup.subList(0, splitGroupOneSize));
        }

        // put the rest of the players into any open team
        for (MiniGamePlayer soloPlayer : soloPlayers) {
            TeamPriority largestSlotTeam = recruitingTeamQueue.remove();
            largestSlotTeam.getTeam().addPlayers(soloPlayer);

            if (largestSlotTeam.getExtraSlotsAvailable(totalPlayers) != 0)
                recruitingTeamQueue.add(largestSlotTeam);
        }
    }

    @Override
    public int getRegisteredTeamsPlayerCount() {
        int size = 0;
        for (TeamPriority priority : priorities)
            size += priority.team.getPlayerCount();
        return size;
    }

    @Override
    public Collection<T> getRegisteredTeams() {
        List<T> teams = new ArrayList<>();
        for (TeamPriority priority : priorities)
            teams.add(priority.team);
        return teams;
    }

    @Override
    public void unregisterTeam(T team) {
        // can iterate normally, even though entry is removed, because stops immediately after removal
        for(int i = 0; i < priorities.size(); i++) {
            if(priorities.get(i).team.equals(team)) {
                priorities.remove(i);
                return;
            }
        }
    }

    protected List<TeamPriority> getPriorities() {
        return priorities;
    }

    protected class TeamPriority {
        private final T team;
        private int priority;

        public TeamPriority(T team, int priority) {
            this.team = team;
            this.priority = priority;
        }

        public T getTeam() {
            return team;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            totalPriority += (priority - this.priority);
            this.priority = priority;
        }

        public int getMaxTeamSize(int playerAmount) {
            return (int) Math.ceil(playerAmount * priority / totalPriority);
        }

        public int getExtraSlotsAvailable(int totalPlayers) {
            return getMaxTeamSize(totalPlayers) - team.getPlayerCount();
        }
    }
}
