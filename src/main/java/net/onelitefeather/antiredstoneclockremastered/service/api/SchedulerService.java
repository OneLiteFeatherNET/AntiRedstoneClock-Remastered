package net.onelitefeather.antiredstoneclockremastered.service.api;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.function.Consumer;

/**
 * Service for scheduling tasks, abstracting away platform-specific details.
 *
 * <p>
 *     This service provides methods to schedule tasks in a way that is compatible with both Folia and Paper platforms.
 * </p>
 *
 * @author TheMeinerLP
 * @since 2.2.0
 * @version 1.0.0
 */
public interface SchedulerService {

    /**
     * Schedules a task to run asynchronously.
     *
     * @param task The task to run.
     */
    void scheduleTask(Runnable task);

    /**
     * Schedules a repeating task to run asynchronously.
     *
     * @param task   The task to run.
     * @param delay  The delay before the first execution, in ticks.
     * @param period The period between successive executions, in ticks.
     */
    void scheduleRepeatingTask(Runnable task, long delay, long period);

    /**
     * Schedules a repeating task to run asynchronously, providing a ScheduledTask reference.
     *
     * @param task   The task to run, which receives a ScheduledTask reference.
     * @param delay  The delay before the first execution, in ticks.
     * @param period The period between successive executions, in ticks.
     * @return A ScheduledTask reference for managing the scheduled task.
     */
    ScheduledTask runTaskTimerAsynchronously(Consumer<ScheduledTask> task, long delay, long period);

    /**
     * Cancels all scheduled tasks.
     */
    void cancelTasks();

}
