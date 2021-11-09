package net.tjalp.peach.melon.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.tjalp.peach.melon.MelonServer
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
}