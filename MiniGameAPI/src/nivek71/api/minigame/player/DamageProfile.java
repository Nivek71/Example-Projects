package nivek71.api.minigame.player;

import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.utility.Helpers;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class DamageProfile implements Iterable<DamageProfile.DamageEntry> {
    public static final int DEFAULT_MAX_LOG_ENTRIES = 10;

    static {
        MiniGamePlugin.registerOnEnableFunction(() -> Helpers.registerDynamicEvent(MiniGamePlugin.getPlugin(), EntityDamageEvent.class,
                DamageProfile::logEntry, EventPriority.MONITOR, true));
    }

    private final ArrayDeque<DamageEntry> entryQueue = new ArrayDeque<>();
    private int maxEntries = DEFAULT_MAX_LOG_ENTRIES;

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public void logEntry(DamageEntry entry) {
        if (entryQueue.size() >= maxEntries)
            entryQueue.remove();
        entryQueue.add(entry);
    }

    public static void logEntry(EntityDamageEvent event) {
        MiniGamePlayer damaged = Lobby.getMiniGamePlayer(event.getEntity());
        if (damaged != null) {
            damaged.getDamageProfile().logEntry(event instanceof EntityDamageByEntityEvent ?
                    new DamageByEntityEntry(event.getDamage(), event.getCause(), Helpers.getAttacker(((EntityDamageByEntityEvent) event).getDamager())) :
                    new DamageEntry(event.getDamage(), event.getCause()));
        }
    }

    @Override
    public Iterator<DamageEntry> iterator() {
        return entryQueue.iterator();
    }

    public DamageEntry[] getEntriesByTime() {
        return entryQueue.toArray(new DamageEntry[0]);
    }

    public DamageEntry[] getEntriesByWeight() {
        DamageEntry[] entries = getEntriesByTime();
        Arrays.sort(entries, Comparator.comparingLong(DamageEntry::getWeight).reversed());
        return entries;
    }

    public DamageEntry getHighestWeightEntry() {
        return Collections.max(entryQueue, Comparator.comparingLong(DamageEntry::getWeight));
    }

    public DamageEntry getMostRecentEntry() {
        return entryQueue.isEmpty() ? null : entryQueue.peek();
    }

    public static class DamageEntry {
        private final long damageMs = System.currentTimeMillis();
        private final double damage;
        private final EntityDamageEvent.DamageCause damageCause;

        public DamageEntry(double damage, EntityDamageEvent.DamageCause damageCause) {
            this.damage = damage;
            this.damageCause = damageCause;
        }

        public long getDamageTimeMs() {
            return damageMs;
        }

        public double getDamage() {
            return damage;
        }

        public EntityDamageEvent.DamageCause getDamageCause() {
            return damageCause;
        }

        public long getWeight() {
            return 1;
        }
    }

    public static class DamageByEntityEntry extends DamageEntry {
        // if a player takes damage from an attacker within 10 seconds, consider it to weigh higher
        public static final int DAMAGE_TIMEOUT_MS = 10000;
        private final LivingEntity attacker;

        public DamageByEntityEntry(double damage, EntityDamageEvent.DamageCause damageCause, LivingEntity attacker) {
            super(damage, damageCause);
            this.attacker = attacker;
        }

        public LivingEntity getAttacker() {
            return attacker;
        }

        @Override
        public long getWeight() {
            long since = System.currentTimeMillis() - getDamageTimeMs();
            return since > DAMAGE_TIMEOUT_MS ? super.getWeight() : DAMAGE_TIMEOUT_MS / since;
        }
    }
}
