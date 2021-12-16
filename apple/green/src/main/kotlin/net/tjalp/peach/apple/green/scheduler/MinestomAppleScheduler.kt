package net.tjalp.peach.apple.green.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.Task
import net.tjalp.peach.apple.pit.scheduler.AppleScheduler
import net.tjalp.peach.apple.pit.scheduler.ReactiveScheduler
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class MinestomAppleScheduler : AppleScheduler() {

    private val scheduler = MinecraftServer.getSchedulerManager()

    override val coroutineContext: CoroutineContext = MinestomDispatcher + SupervisorJob()
    override val reactive = ReactiveScheduler(this)

    override fun runTask(cancel: Boolean, cb: suspend (SchedulerTask) -> Unit): SchedulerTask {
        if (disposed) {
            throw IllegalStateException("Attempted runTask on disposed scheduler")
        }

        val task = if (cancel) {
            MinestomCancelableTask(cb)
        } else {
            MinestomSchedulerTask()
        } as MinestomSchedulerTask

        val sched = scheduler.buildTask {
            launch(Dispatchers.Unconfined) {
                try {
                    cb(task)
                } catch (err: Exception) {
                    err.printStackTrace()
                }
            }
        }.schedule()

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
            MinestomCancelableTask(cb)
        } else {
            MinestomSchedulerTask()
        } as MinestomSchedulerTask

        val sched = scheduler.buildTask {
            launch(Dispatchers.Unconfined) {
                try {
                    cb(task)
                } catch (err: Exception) {
                    err.printStackTrace()
                }
            }
        }.delay(delay).schedule()

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
            throw IllegalStateException("Attempted repeatTask on disposed scheduler")
        }

        val task = if (onCancel != null) {
            MinestomCancelableTask(cb)
        } else {
            MinestomSchedulerTask()
        } as MinestomSchedulerTask

        val sched = scheduler.buildTask {
            launch(Dispatchers.Unconfined) {
                try {
                    cb(task)
                } catch (err: Exception) {
                    err.printStackTrace()
                }
            }
        }.delay(delay).repeat(repeat).schedule()

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
            !(it as MinestomSchedulerTask).ref.isAlive
        }

        lastCleanTime = System.currentTimeMillis()
    }

    class MinestomCancelableTask(
        onCancel: suspend (SchedulerTask) -> Unit
    ) : CancelableTask(onCancel) {

        override val dispatcher: CoroutineDispatcher = MinestomDispatcher
    }

    class MinestomSchedulerTask : SchedulerTask() {

        lateinit var ref: Task

        override fun dispose() {
            ref.cancel()
        }

        override fun isDisposed(): Boolean {
            return !ref.isAlive
        }
    }
}