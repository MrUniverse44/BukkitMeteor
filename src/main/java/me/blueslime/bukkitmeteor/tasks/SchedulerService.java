package me.blueslime.bukkitmeteor.tasks;

import me.blueslime.bukkitmeteor.implementation.module.Service;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerService implements Service {

    /**
     * Execute scheduler task runnable
     * @param runnable to execute
     * @param settings of the task
     * @return running task
     */
    public BukkitTask execute(Runnable runnable, TaskSettings settings) {
        JavaPlugin plugin = getMeteorPlugin();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        switch (settings.getType()) {
            default -> {
                if (settings.isAsync()) {
                    return scheduler.runTaskAsynchronously(
                            plugin,
                            runnable
                    );
                } else {
                    return scheduler.runTask(
                            plugin,
                            runnable
                    );
                }
            }
            case LATER -> {
                if (settings.isAsync()) {
                    return scheduler.runTaskLaterAsynchronously(
                            plugin,
                            runnable,
                            settings.getDelay()
                    );
                } else {
                    return scheduler.runTaskLater(
                            plugin,
                            runnable,
                            settings.getDelay()
                    );
                }
            }
            case TIMER -> {
                if (settings.isAsync()) {
                    return scheduler.runTaskTimerAsynchronously(
                            plugin,
                            runnable,
                            settings.getDelay(),
                            settings.getPeriod()
                    );
                } else {
                    return scheduler.runTaskTimer(
                            plugin,
                            runnable,
                            settings.getDelay(),
                            settings.getPeriod()
                    );
                }
            }
        }
    }

}
