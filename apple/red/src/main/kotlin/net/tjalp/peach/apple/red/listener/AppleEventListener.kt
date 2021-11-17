package net.tjalp.peach.apple.red.listener

import kotlinx.coroutines.runBlocking
import net.tjalp.peach.apple.red.PaperAppleServer
import net.tjalp.peach.apple.red.util.register
import net.tjalp.peach.proto.apple.Apple
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

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
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val player = event.player
        val request = Apple.PlayerHandshakeRequest.newBuilder()
            .setUuid(player.uniqueId.toString())
            .setPlayerName(player.name)
            .build()

        runBlocking {
            server.rpcStub.playerHandshake(request)
        }
    }
}