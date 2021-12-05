package net.tjalp.peach.melon.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.tjalp.peach.melon.MelonServer
import net.tjalp.peach.proto.melon.Melon
import java.util.*

class MelonEventListener(
    private val melon: MelonServer
) {

    private val initialAppleNodeCache = HashMap<UUID, String>()

    init {
        melon.proxy.eventManager.register(melon, this)
    }

    @Subscribe
    private fun onPlayerChooseInitialServer(event: PlayerChooseInitialServerEvent) {
        val nodeId = initialAppleNodeCache.remove(event.player.uniqueId)
        val server = melon.proxy.getServer(nodeId)

        if (server.isEmpty) {
            event.setInitialServer(null)
            return
        }

        event.setInitialServer(server.get())
    }

    @Subscribe
    private fun onLogin(event: LoginEvent) {
        val request = Melon.PlayerHandshakeRequest.newBuilder()
            .setUniqueId(event.player.uniqueId.toString())
            .setUsername(event.player.username)
            .build()

        runBlocking {
            val response = melon.rpcStub.playerHandshake(request)

            initialAppleNodeCache[event.player.uniqueId] = response.targetNodeIdentifier
        }
    }

    @Subscribe
    private fun onKickedFromServer(event: KickedFromServerEvent) {
        if (!event.kickedDuringServerConnect()) {
            val server = if (event.server == null) {
                melon.proxy.allServers.randomOrNull()
            } else {
                melon.proxy.allServers
                    .filter {
                        it.serverInfo.name != event.server.serverInfo.name
                    }
                    .randomOrNull()
            }

            event.result = KickedFromServerEvent.RedirectPlayer.create(server)
        }
    }

    @Subscribe
    private fun onDisconnect(event: DisconnectEvent) {
        val request = Melon.PlayerQuit.newBuilder()
            .setUniqueId(event.player.uniqueId.toString())
            .build()

        GlobalScope.launch {
            melon.rpcStub.playerDisconnect(request)
        }
    }
}