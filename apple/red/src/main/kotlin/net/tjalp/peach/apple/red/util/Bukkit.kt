package net.tjalp.peach.apple.red.util

import net.tjalp.peach.apple.pit.AppleServer
import net.tjalp.peach.apple.red.PaperAppleServer
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

//
// Bukkit extension methods for convenience
//

/**
 * Register the listener
 *
 * @return self
 */
fun Listener.register(): Listener {
    Bukkit.getPluginManager().registerEvents(this, (AppleServer.get() as PaperAppleServer).plugin)
    return this
}

/**
 * Unregister the listener
 */
fun Listener.unregister() {
    HandlerList.unregisterAll(this)
}