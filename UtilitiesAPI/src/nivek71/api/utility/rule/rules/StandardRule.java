package nivek71.api.utility.rule.rules;

import nivek71.api.utility.UtilityPlugin;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class StandardRule extends Rule {
    public static final Rule ENTITY_HUNGER = new Rule().addEntityRuleEnforcer(UtilityPlugin.getPlugin(), FoodLevelChangeEvent.class, true);
    public static final Rule ENTITY_DAMAGE = new Rule().addEntityRuleEnforcer(UtilityPlugin.getPlugin(), EntityDamageEvent.class, true);
    public static final Rule ENTITY_DAMAGE_FROM_ENTITY = new Rule(ENTITY_DAMAGE).addEntityRuleEnforcer(UtilityPlugin.getPlugin(), EntityDamageByEntityEvent.class, true);
    public static final Rule ENTITY_DAMAGE_FROM_BLOCK = new Rule(ENTITY_DAMAGE).addEntityRuleEnforcer(UtilityPlugin.getPlugin(), EntityDamageByBlockEvent.class, true);
    public static final ReSpawnRule RESPAWN_NEVER = new ReSpawnRule(-1);
    public static final ReSpawnRule RESPAWN_IMMEDIATELY = new ReSpawnRule(0);

    public static ReSpawnRule RESPAWN_AFTER(int delay) {
        return new ReSpawnRule(delay);
    }

    public static final Rule PLAYER_MODIFY_BLOCK = new Rule();
    public static final Rule PLAYER_BREAK_BLOCK = new Rule(PLAYER_MODIFY_BLOCK).addRuleEnforcer(UtilityPlugin.getPlugin(), BlockBreakEvent.class, true, BlockBreakEvent::getPlayer, event -> event.setCancelled(true));
    public static final Rule PLAYER_PLACE_BLOCK = new Rule(PLAYER_MODIFY_BLOCK).addRuleEnforcer(UtilityPlugin.getPlugin(), BlockPlaceEvent.class, true, BlockPlaceEvent::getPlayer, event -> event.setCancelled(true));
    public static final Rule PLAYER_QUEUE_KIT = new Rule();
    public static final Rule PLAYER_APPLY_KIT = new Rule();

    public static class ReSpawnRule extends RuleVariation {
        private final int reSpawnDelay;

        public ReSpawnRule(int reSpawnDelay) {
            this.reSpawnDelay = reSpawnDelay;
        }

        public int getReSpawnDelay() {
            return reSpawnDelay;
        }

        @Override
        public String toString() {
            return "ReSpawnRule{" + reSpawnDelay + '}';
        }
    }

    private StandardRule() {
    }
}
