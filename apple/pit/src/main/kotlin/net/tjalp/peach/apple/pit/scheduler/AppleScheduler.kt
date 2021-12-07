package net.tjalp.peach.apple.pit.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import reactor.core.Disposable
import reactor.core.Disposables
import reactor.core.scheduler.Scheduler
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

abstract class AppleScheduler<T : ReactiveScheduler> : Executor, CoroutineScope, Scheduler, Disposable {

    var disposed = false
    val taskList: MutableList<SchedulerTask> = Collections.synchronizedList(ArrayList())
    private val composite = Disposables.composite()

    /**
     * A free to use list of composite resources
     */
    val disposables: Disposable.Composite
        get() = composite

    /**
     * Run the given task directly on the main thread
     *
     * @param cancel cancel if the callback should be run when the source is disposed
     * @param cb The callback to execute
     * @return Task instance
     */
    abstract fun runTask(cancel: Boolean = false, cb: suspend (SchedulerTask) -> Unit): SchedulerTask

    /**
     * Delay a task and execute it after the given amount
     * of ticks have passed.
     *
     * @param ticks The ticks to delay for
     * @param cancel if the callback should be run when the source is disposed
     * @param cb The callback to execute
     * @return Task instance
     */
    abstract fun delayTask(ticks: Long, cancel: Boolean = false, cb: suspend (SchedulerTask) -> Unit): SchedulerTask

    /**
     * Delay a task and execute it after the given amount
     * of ticks have passed. The task will be re-executed
     * periodically after the amount of ticks have passed.
     *
     * @param delay The ticks to delay for
     * @param ticks The ticks until repeated
     * @param onCancel Optional cancel callback
     * @param cb The callback to execute
     * @return Task instance
     */
    abstract fun repeatTask(
        delay: Long,
        ticks: Long,
        onCancel: (suspend (SchedulerTask) -> Unit)? = null,
        cb: suspend (SchedulerTask) -> Unit
    ): SchedulerTask

    /**
     * Simple executor interfacing used to schedule the
     * runnable to run on the main thread directly.
     */
    override fun execute(command: Runnable) {
        runTask {
            command.run()
        }
    }

    /**
     * Periodically clean up expired tasks
     */
    abstract fun purgeExpiredTasks(force: Boolean)

    override fun dispose() {
        if (disposed) return

        disposed = true


        composite.dispose()
    }

    override fun isDisposed(): Boolean = disposed

    // ----- Scheduler delegators -----

    abstract val reactive: T

    override fun schedule(task: Runnable, delay: Long, unit: TimeUnit): Disposable {
        return reactive.schedule(task, delay, unit)
    }

    override fun schedulePeriodically(task: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): Disposable {
        return reactive.schedulePeriodically(task, initialDelay, period, unit)
    }

    override fun schedule(task: Runnable): Disposable = reactive.schedule(task)

    override fun createWorker(): Scheduler.Worker = reactive.createWorker()

    /**
     * Represents a task that can be cancelled in the future
     */
    abstract class CancelableTask(
        val onCancel: suspend (SchedulerTask) -> Unit
    ) : SchedulerTask() {

        abstract val dispatcher: CoroutineDispatcher

        override fun dispose() {
            super.invoke()

            // Always execute cancelations in the global
            // scope to assure they are fully run.
            GlobalScope.launch(dispatcher) {
                onCancel(this@CancelableTask)
            }
        }

    }

    /**
     * A task that has been scheduled to run in
     * the future, either once or multiple times.
     */
    abstract class SchedulerTask : Disposable {

        operator fun invoke() = dispose()
    }
}