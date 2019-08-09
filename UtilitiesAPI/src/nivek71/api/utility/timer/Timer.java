package nivek71.api.utility.timer;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Timer {
    public static final int TICKS_PER_SECOND = 20;
    private static final Map<Object, Set<Timer>> objectTimerAssociations = new HashMap<>();
    private final Plugin plugin;

    public Timer(Plugin plugin, Object...associations) {
        this.plugin = plugin;
        createAssociations(associations);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public abstract void cancel();
    protected abstract BukkitTask schedule(BukkitRunnable runnable);

    public void createAssociations(Object... objects) {
        for (Object object : objects)
            objectTimerAssociations.computeIfAbsent(object, k -> new HashSet<>()).add(this);
    }

    public static void cancelAssociated(Object object) {
        Set<Timer> timers = objectTimerAssociations.remove(object);
        if (timers != null)
            timers.forEach(Timer::cancel);
    }
}
