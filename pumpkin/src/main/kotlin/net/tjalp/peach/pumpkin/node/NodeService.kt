package net.tjalp.peach.pumpkin.node

import net.tjalp.peach.pumpkin.PumpkinMainThread
import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.apple.AppleNode
import net.tjalp.peach.pumpkin.node.apple.AppleService
import net.tjalp.peach.pumpkin.node.melon.MelonNode
import net.tjalp.peach.pumpkin.node.melon.MelonService
import net.tjalp.peach.pumpkin.player.ConnectedPlayer

class NodeService(
    private val pumpkin: PumpkinServer
) {

    /**
     * All active melon nodes are in this list
     */
    val melonNodes: Set<MelonNode>
        get() = registeredMelonNodes

    /**
     * All active apple nodes are in this list
     */
    val appleNodes: Set<AppleNode>
        get() = registeredAppleNodes

    /**
     * Returns a collection of all nodes
     */
    val nodes: Collection<Node>
        get() = ArrayList<Node>().also {
            it.addAll(melonNodes)
            it.addAll(appleNodes)
        }

    private val registeredMelonNodes = mutableSetOf<MelonNode>()
    private val registeredAppleNodes = mutableSetOf<AppleNode>()

    private val thread: PumpkinMainThread = pumpkin.mainThread

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

    /**
     * Register a melon node
     *
     * @param node The melon node
     */
    fun register(node: MelonNode) {
        thread.ensureMainThread()
        pumpkin.logger.info("Registering melon node (nodeId: ${node.nodeId})")

        // Register the players that are already on this melon node
        node.players.forEach {
            pumpkin.playerService.register(
                it.uniqueId,
                it.username,
                it.currentMelonNode,
                it.currentAppleNode
            )
        }

        registeredMelonNodes.add(node)
    }

    /**
     * Register an apple node
     *
     * @param node The apple node
     */
    fun register(node: AppleNode) {
        thread.ensureMainThread()
        registeredAppleNodes.add(node)
    }

    /**
     * Unregister a melon node
     *
     * @param node The melon node to unregister
     */
    fun unregister(node: MelonNode) {
        thread.ensureMainThread()
        pumpkin.logger.info("Unregistering melon node (nodeId: ${node.nodeId})")

        // Unregister all the players that are on this melon node
        node.players.forEach {
            pumpkin.playerService.unregister(it as ConnectedPlayer)
        }

        registeredMelonNodes.remove(node)
    }

    /**
     * Unregister an apple node
     *
     * @param node The apple node the unregister
     */
    fun unregister(node: AppleNode) {
        thread.ensureMainThread()
        registeredAppleNodes.remove(node)
    }

    /**
     * Get a melon node off of a node identifier
     *
     * @param nodeId The node identifier
     * @return The melon node associated with the node identifier
     */
    fun getMelonNode(nodeId: String): MelonNode? {
        thread.ensureMainThread()

        return registeredMelonNodes.firstOrNull {
            it.nodeId == nodeId
        }
    }

    /**
     * Get an apple node off of a node identifier
     *
     * @param nodeId The node identifier
     * @return The apple node associated with the node identifier
     */
    fun getAppleNode(nodeId: String): AppleNode? {
        thread.ensureMainThread()

        return registeredAppleNodes.firstOrNull {
            it.nodeId == nodeId
        }
    }
}