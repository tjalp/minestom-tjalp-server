package net.tjalp.peach.apple.red.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.tjalp.peach.apple.pit.scheduler.AppleScheduler
import net.tjalp.peach.apple.red.PaperAppleServer
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToLong

class PaperAppleScheduler(
    private val apple: PaperAppleServer
) : AppleScheduler<PaperReactiveScheduler>() {

    private val scheduler = Bukkit.getScheduler()

    override val coroutineContext: CoroutineContext = PaperDispatcher + SupervisorJob()
    override val reactive: PaperReactiveScheduler = PaperReactiveScheduler()

    override fun runTask(cancel: Boolean, cb: suspend (SchedulerTask) -> Unit): SchedulerTask {
        if (disposed) {
            throw IllegalStateException("Attempted runTask on disposed scheduler")
        }

        val task = if (cancel) {
            PaperCancelableTask(cb)
        } else {
            PaperSchedulerTask()
        } as PaperSchedulerTask

        val sched = scheduler.runTask(apple.plugin, Runnable {
            launch(Dispatchers.Unconfined) {
                try {
                    cb(task)
                } catch (err: Exception) {
                    err.printStackTrace()
                }
            }
        })

        purgeExpiredTasks(false)
        task.ref = sched
        taskList.add(task)

        return task
    }

    override fun delayTask(delay: Duration, cancel: Boolean, cb: suspend (SchedulerTask) -> Unit): SchedulerTask {
        if (disposed) {
            throw IllegalStateException("Attempted delayTask on disposed scheduler")
        }

        val task = if (cancel) {
            PaperCancelableTask(cb)
        } else {
            PaperSchedulerTask()
        } as PaperSchedulerTask

        val sched = scheduler.runTaskLater(apple.plugin, Runnable {
            launch(Dispatchers.Unconfined) {
                try {
                    cb(task)
                } catch (err: Exception) {
                    err.printStackTrace()
                }
            }
        }, toTicks(delay))

        purgeExpiredTasks(false)
        task.ref = sched
        taskList.add(task)

        return task
    }

    override fun repeatTask(
        delay: Duration,
        repeat: Duration,
        onCancel: (suspend (SchedulerTask) -> Unit)?,
        cb: suspend (SchedulerTask) -> Unit
    ): SchedulerTask {
        if (disposed) {
            throw IllegalStateException("Attempted delayTask on disposed scheduler")
        }

        val task = if (onCancel != null) {
            PaperCancelableTask(cb)
        } else {
            PaperSchedulerTask()
        } as PaperSchedulerTask

        val sched = scheduler.runTaskTimer(apple.plugin, Runnable {
            launch(Dispatchers.Unconfined) {
                try {
                    cb(task)
                } catch (err: Exception) {
                    err.printStackTrace()
                }
            }
        }, toTicks(delay), toTicks(repeat))

        purgeExpiredTasks(false)
        task.ref = sched
        taskList.add(task)

        return task
    }

    override fun purgeExpiredTasks(force: Boolean) {
        if (!force && System.currentTimeMillis() - lastCleanTime < 1000) {
            return
        }

        taskList.removeIf {
            !scheduler.isCurrentlyRunning((it as PaperSchedulerTask).ref.taskId) && !scheduler.isQueued(it.ref.taskId)
        }

        lastCleanTime = System.currentTimeMillis()
    }

    /**
     * Convert a [Duration] to ticks
     */
    private fun toTicks(duration: Duration): Long {
        return (duration.toMillis().toDouble() / 50.0).roundToLong()
    }

    class PaperCancelableTask(
        onCancel: suspend (SchedulerTask) -> Unit
    ) : CancelableTask(onCancel) {

        override val dispatcher: CoroutineDispatcher = PaperDispatcher
    }

    class PaperSchedulerTask : SchedulerTask() {

        lateinit var ref: BukkitTask

        override fun dispose() {
            ref.cancel()
        }

        override fun isDisposed(): Boolean {
            return ref.isCancelled
        }

    }
}