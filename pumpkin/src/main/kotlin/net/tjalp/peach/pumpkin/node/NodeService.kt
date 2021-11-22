package net.tjalp.peach.pumpkin.node

import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.apple.AppleNode
import net.tjalp.peach.pumpkin.node.apple.AppleService
import net.tjalp.peach.pumpkin.node.melon.MelonNode
import net.tjalp.peach.pumpkin.node.melon.MelonService

class NodeService(
    private val pumpkin: PumpkinServer
) {

    /**
     * All active apple nodes are in this list
     */
    val appleNodes = mutableListOf<AppleNode>()

    /**
     * All active melon nodes are in this list
     */
    val melonNodes = mutableListOf<MelonNode>()

    /**
     * Returns a collection of all nodes
     */
    val nodes: Collection<Node>
        get() = ArrayList<Node>().also {
            it.addAll(appleNodes)
            it.addAll(melonNodes)
        }

    /**
     * Initialize the registry
     */
    fun setup() {
        pumpkin.logger.info("Setting up node registry")

        pumpkin.rpcService.configure {
            it.addService(AppleService(pumpkin))
            it.addService(MelonService(pumpkin))
        }
    }
}