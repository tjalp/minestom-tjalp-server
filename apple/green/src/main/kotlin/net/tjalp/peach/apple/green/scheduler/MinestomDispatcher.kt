package net.tjalp.peach.apple.green.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import net.minestom.server.MinecraftServer
import kotlin.coroutines.CoroutineContext

object MinestomDispatcher : CoroutineDispatcher() {

    private val scheduler = MinecraftServer.getSchedulerManager()

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Thread.currentThread().name.equals("main")
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        scheduler.buildTask {
            block.run()
        }.schedule()
    }
}