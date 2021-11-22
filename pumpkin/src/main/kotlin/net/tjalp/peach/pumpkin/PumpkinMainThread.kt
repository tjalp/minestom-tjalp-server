package net.tjalp.peach.pumpkin

import io.grpc.Context
import reactor.core.Disposable
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.concurrent.*

class PumpkinMainThread private constructor(
    private val mainExecutor: ExecutorService,
    private val asyncExecutor: ExecutorService,
    private val mainScheduler: Scheduler
) : Scheduler {

    private val scheduler = ScheduledThreadPoolExecutor(1)

    init {
        scheduler.removeOnCancelPolicy = true
    }

    /**
     * Returns true only when the current thread
     * is the Captain main thread.
     */
    private val isMainThread: Boolean
        get() = Thread.currentThread().name == MAIN_THREAD_NAME

    /**
     * Push a new task to the main executor
     *
     * @param task The task to run
     */
    fun syncTask(task: Runnable) {
        if (isMainThread) {
            task.run()
        } else {
            mainExecutor.submit(
                Context.current().wrap(task)
            ).get()
        }
    }

    /**
     * Push a new task to the async executor
     *
     * @param task The task to run
     * @return The task future
     */
    fun asyncTask(task: Runnable): Future<*> {
        return asyncExecutor.submit(
            Context.current().wrap(task)
        )
    }

    /**
     * Push a new task to the main executor
     * and return the resolved value.
     *
     * @param task The task to run
     */
    fun <T> awaitTask(task: Callable<T>): T {
        return if (isMainThread) {
            task.call()
        } else {
            mainExecutor.submit(
                Context.current().wrap(task)
            ).get()
        }
    }

    /**
     * Delay the given task by the specified duration
     * and execute it on the main thread once
     * the delay has elapsed.
     *
     * @param delay The time to wait
     * @param task The task to run
     * @return ScheduledFuture the scheduled future
     */
    fun delayTask(delay: Duration, task: Runnable): ScheduledFuture<*> {
        val delegate = Context.current().wrap {
            syncTask(task)
        }

        return scheduler.schedule(
            delegate,
            delay.toMillis(),
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * Schedule the given task by the specified duration
     * and execute it on the main thread once
     * the delay has elapsed, and repeat at the given interval.
     *
     * @param delay The time to wait
     * @param interval The interval time
     * @param task The task to run
     * @return The ScheduledFuture instance
     */
    fun scheduleTask(delay: Duration, interval: Duration, task: Runnable): ScheduledFuture<*> {
        val delegate = Context.current().wrap {
            syncTask(task)
        }

        return scheduler.scheduleAtFixedRate(
            delegate,
            delay.toMillis(),
            interval.toMillis(),
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * Require a task to be run from the main
     * Captain worker thread.
     *
     * @throws IllegalStateException If the thread is not main
     */
    fun ensureMainThread() {
        if (!isMainThread) {
            throw IllegalStateException("Thread is not main (found ${Thread.currentThread().name})")
        }
    }

    /**
     * Shutdown this main thread instance
     */
    fun shutdown() {
        scheduler.shutdown()
        scheduler.awaitTermination(5, TimeUnit.SECONDS)
        mainExecutor.shutdown()
        mainExecutor.awaitTermination(5, TimeUnit.SECONDS)
        asyncExecutor.shutdown()
        asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)
        mainScheduler.dispose()
    }

    override fun schedule(task: Runnable): Disposable {
        return mainScheduler.schedule(task)
    }

    override fun schedule(task: Runnable, delay: Long, unit: TimeUnit): Disposable {
        return mainScheduler.schedule(task, delay, unit)
    }

    override fun schedulePeriodically(task: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): Disposable {
        return mainScheduler.schedulePeriodically(task, initialDelay, period, unit)
    }

    override fun createWorker(): Scheduler.Worker {
        return mainScheduler.createWorker()
    }

    /**
     * The Captain master thread factory
     */
    private class PumpkinMainThreadFactory : ThreadFactory {

        override fun newThread(task: Runnable): Thread {
            return Thread(task).apply {
                name = MAIN_THREAD_NAME
            }
        }
    }

    companion object {
        const val MAIN_THREAD_NAME = "Pumpkin Main Thread"

        /**
         * Create a new [PumpkinMainThread]
         */
        fun create(): PumpkinMainThread {
            val executor = Executors.newSingleThreadExecutor(PumpkinMainThreadFactory())
            val offthread = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
            val scheduler = Schedulers.fromExecutor(executor)

            return PumpkinMainThread(executor, offthread, scheduler)
        }
    }
}