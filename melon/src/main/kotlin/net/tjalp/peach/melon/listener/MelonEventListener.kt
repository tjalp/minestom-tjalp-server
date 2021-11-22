package net.tjalp.peach.melon.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.proxy.server.RegisteredServer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.tjalp.peach.melon.MelonServer
import net.tjalp.peach.proto.melon.Melon
import java.util.*

class MelonEventListener(
    private val melon: MelonServer
) {

    @Subscribe
    private fun onPlayerChooseInitialServer(event: PlayerChooseInitialServerEvent) {
        val server: Optional<RegisteredServer> = melon.proxy.getServer("apple")

        if (server.isPresent) {
            event.setInitialServer(server.get())
            return
        }

        event.setInitialServer(null)
    }

    @Subscribe
    private fun onLogin(event: LoginEvent) {
        val request = Melon.PlayerHandshakeRequest.newBuilder()
            .setUniqueId(event.player.uniqueId.toString())
            .setUsername(event.player.username)
            .build()

        runBlocking {
            melon.rpcStub.playerHandshake(request)
        }
    }

    @Subscribe
    private fun onDisconnect(event: DisconnectEvent) {
        val request = Melon.PlayerQuit.newBuilder()
            .setUniqueId(event.player.uniqueId.toString())
            .build()

        GlobalScope.async {
            melon.rpcStub.playerDisconnect(request)
        }
    }
}