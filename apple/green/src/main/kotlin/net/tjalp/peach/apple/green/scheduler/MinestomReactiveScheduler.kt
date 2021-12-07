package net.tjalp.peach.apple.green.scheduler

import net.tjalp.peach.apple.pit.scheduler.ReactiveScheduler
import reactor.core.Disposable
import reactor.core.scheduler.Scheduler

class MinestomReactiveScheduler : ReactiveScheduler {

    override fun schedule(task: Runnable): Disposable {
        TODO("Not yet implemented")
    }

    override fun createWorker(): Scheduler.Worker {
        TODO("Not yet implemented")
    }
}