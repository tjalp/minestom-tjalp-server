package net.tjalp.peach.pumpkin.node

import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.apple.AppleService
import net.tjalp.peach.pumpkin.node.melon.MelonService

class NodeService(
    private val pumpkin: PumpkinServer
) {

    fun setup() {
        pumpkin.logger.info("Setting up node registry")

        pumpkin.rpcService.configure {
            it.addService(AppleService(pumpkin))
            it.addService(MelonService(pumpkin))
        }
    }
}