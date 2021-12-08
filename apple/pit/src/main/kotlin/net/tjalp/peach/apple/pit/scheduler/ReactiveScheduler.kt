package net.tjalp.peach.apple.pit.scheduler

import reactor.core.Disposable
import reactor.core.Disposables
import reactor.core.scheduler.Scheduler
import java.time.Duration
import java.util.concurrent.TimeUnit

open class ReactiveScheduler(
    val scheduler: AppleScheduler
) : Scheduler {

    override fun schedule(task: Runnable): Disposable {
        try {
            return scheduler.runTask {
                task.run()
            }
        } catch (err: Exception) {
            throw err
        }
    }

    override fun schedule(task: Runnable, delay: Long, unit: TimeUnit): Disposable {
        try {
            return scheduler.delayTask(toDuration(delay, unit)) {
                task.run()
            }
        } catch(err: Exception) {
            throw err
        }
    }

    override fun schedulePeriodically(task: Runnable, initialDelay: Long, period: Long, unit: TimeUnit): Disposable {
        try {
            return scheduler.repeatTask(toDuration(initialDelay, unit), toDuration(period, unit)) {
                task.run()
            }
        } catch(err: Exception) {
            throw err
        }
    }

    override fun createWorker(): Scheduler.Worker = AppleWorker()

    /**
     * Convert a time with [TimeUnit] to a [Duration]
     *
     * @param time The time
     * @param unit The [TimeUnit]
     * @return The result [Duration]
     */
    private fun toDuration(time: Long, unit: TimeUnit): Duration { // TODO Improve this lol
        return Duration.ofMillis(unit.toMillis(time))
    }

    inner class AppleWorker : Scheduler.Worker {

        private val composite = Disposables.composite()

        override fun schedule(task: Runnable): Disposable {
            try {
                return scheduler.runTask {
                    task.run()
                    composite.remove(it)
                }.also {
                    composite.add(it)
                }
            } catch(err: Exception) {
                throw err
            }
        }

        override fun schedule(task: Runnable, delay: Long, unit: TimeUnit): Disposable {
            try {
                return scheduler.delayTask(toDuration(delay, unit)) {
                    task.run()
                    composite.remove(it)
                }.also {
                    composite.add(it)
                }
            } catch(err: Exception) {
                throw err
            }
        }

        override fun schedulePeriodically(task: Runnable, initial: Long, repeat: Long, unit: TimeUnit): Disposable {
            try {
                return scheduler.repeatTask(toDuration(initial, unit), toDuration(repeat, unit), {
                    composite.remove(it)
                }) {
                    task.run()
                }.also {
                    composite.add(it)
                }
            } catch(err: Exception) {
                throw err
            }
        }

        override fun dispose() {
            composite.dispose()
        }

        override fun isDisposed(): Boolean {
            return composite.isDisposed
        }

    }

}