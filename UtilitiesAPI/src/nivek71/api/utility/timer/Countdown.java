package nivek71.api.utility.timer;

import nivek71.api.utility.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Countdown extends Timer {
    private BukkitTask task;
    private int ticksPerInterval;
    private int intervalsRemaining;

    public Countdown(Plugin plugin, int ticks, int ticksPerInterval, boolean startNow, Object... associations) {
        super(plugin, associations);
        this.ticksPerInterval = ticksPerInterval;
        intervalsRemaining = (int) Math.ceil((float) ticks / ticksPerInterval); // will run slightly over if not even
        if (startNow)
            start();
    }

    public Countdown(Plugin plugin, int seconds, boolean startNow, Object... associations) {
        this(plugin, seconds * TICKS_PER_SECOND, TICKS_PER_SECOND, startNow, associations);
    }

    protected void onInterval() {
        // do nothing by default
    }

    protected void onEnd() {
        // do nothing by default
    }

    public void start() {
        if (task != null) cancel();

        if (0 >= intervalsRemaining) {
            Logger.tryOrLog(this::onEnd);
        } else task = schedule(new BukkitRunnable() {
            @Override
            public void run() {
                Logger.tryOrLog(Countdown.this::onInterval);
                if (intervalsRemaining-- <= 0) {
                    Logger.tryOrLog(Countdown.this::onEnd);
                    Countdown.this.cancel();
                }
            }
        });
    }

    @Override
    protected BukkitTask schedule(BukkitRunnable runnable) {
        return runnable.runTaskTimer(getPlugin(), ticksPerInterval, ticksPerInterval);
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public int getRemainingIntervals() {
        return intervalsRemaining;
    }

    public void setRemainingIntervals(int intervals) {
        this.intervalsRemaining = intervals;
    }
}
