package net.tjalp.peach.apple.red.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import net.tjalp.peach.apple.pit.AppleServer
import net.tjalp.peach.apple.red.PaperAppleServer
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

object PaperDispatcher : CoroutineDispatcher() {

    private val apple = AppleServer.get() as PaperAppleServer

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Bukkit.isPrimaryThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        Bukkit.getScheduler().runTask(apple.plugin, block)
    }
}