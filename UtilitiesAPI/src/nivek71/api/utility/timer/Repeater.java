package nivek71.api.utility.timer;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Repeater extends Timer {
    private final int period;
    private BukkitTask task;

    public Repeater(Plugin plugin, int period, boolean startNow, Object...associations) {
        super(plugin, associations);
        this.period = period;

        if (startNow)
            start();
    }

    public void onRepeat() {
        // do nothing by default
    }

    public void start() {
        if (task != null) cancel();
        task = schedule(new BukkitRunnable() {
            @Override
            public void run() {
                onRepeat();
            }
        });
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    protected BukkitTask schedule(BukkitRunnable runnable) {
        return runnable.runTaskTimer(getPlugin(), period, period);
    }
}
