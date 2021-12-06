package net.tjalp.peach.apple.red.listener

import net.tjalp.peach.apple.red.PaperAppleServer
import net.tjalp.peach.apple.red.util.register
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * The main event listener
 */
class AppleEventListener(
    private val server: PaperAppleServer
) : Listener {

    init {
        register()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.isOp = true
    }
}