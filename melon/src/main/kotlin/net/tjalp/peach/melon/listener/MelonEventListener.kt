package net.tjalp.peach.melon.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.ConnectionHandshakeEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.proxy.server.RegisteredServer
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
            .setUuid(event.player.uniqueId.toString())
            .setPlayerName(event.player.username)
            .build()

        melon.rpcFutureStub.playerHandshake(request)
    }
}