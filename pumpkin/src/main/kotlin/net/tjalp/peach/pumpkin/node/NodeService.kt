package net.tjalp.peach.pumpkin.node

import net.tjalp.peach.peel.APPLE_NODE_REGISTER
import net.tjalp.peach.peel.APPLE_NODE_UNREGISTER
import net.tjalp.peach.peel.REQUEST_PUMPKIN_CONNECT
import net.tjalp.peach.peel.signal.AppleNodeRegisterSignal
import net.tjalp.peach.peel.signal.AppleNodeUnregisterSignal
import net.tjalp.peach.pumpkin.PumpkinMainThread
import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.apple.AppleNode
import net.tjalp.peach.pumpkin.node.apple.AppleService
import net.tjalp.peach.pumpkin.node.melon.MelonNode
import net.tjalp.peach.pumpkin.node.melon.MelonService
import net.tjalp.peach.pumpkin.player.ConnectedPlayer
import java.time.Duration

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

        val heartbeatInterval = Duration.ofSeconds(5)

        pumpkin.rpcService.configure {
            it.addService(AppleService(pumpkin))
            it.addService(MelonService(pumpkin))
        }

        // Send connection heartbeat
        pumpkin.mainThread.scheduleTask(heartbeatInterval, heartbeatInterval) {
            pumpkin.redis.publish(REQUEST_PUMPKIN_CONNECT).subscribe()
        }
    }

    /**
     * Register a melon node
     *
     * @param node The melon node
     */
    fun register(node: MelonNode) {
        thread.ensureMainThread()
        pumpkin.logger.info("Registering melon node (nodeId: ${node.nodeIdentifier}, port: ${node.port})")

        if (!node.dockerNode.usedPorts.add(node.port)) {
            pumpkin.logger.error("The port ${node.port} is not available, yet a melon node tried to register with it!")

            node.dockerNode.killNode(node)
            return
        }

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
        pumpkin.logger.info("Registering apple node (nodeId: ${node.nodeIdentifier}, server: ${node.server}, port: ${node.port})")

        if (!node.dockerNode.usedPorts.add(node.port)) {
            pumpkin.logger.error("The port ${node.port} is not available, yet an apple node tried to register with it!")

            node.dockerNode.killNode(node)
            return
        }

        registeredAppleNodes.add(node)

        // Send out redis signal
        val signal = AppleNodeRegisterSignal().apply {
            nodeIdentifier = node.nodeIdentifier
            server = node.server
            port = node.port
        }

        pumpkin.redis.publish(APPLE_NODE_REGISTER, signal).subscribe()
    }

    /**
     * Unregister a melon node
     *
     * @param node The melon node to unregister
     */
    fun unregister(node: MelonNode) {
        thread.ensureMainThread()
        pumpkin.logger.info("Unregistering melon node (nodeId: ${node.nodeIdentifier})")

        // Unregister all the players that are on this melon node
        node.players.forEach {
            pumpkin.playerService.unregister(it as ConnectedPlayer)
        }

        node.dockerNode.usedPorts -= node.port
        registeredMelonNodes.remove(node)
    }

    /**
     * Unregister an apple node
     *
     * @param node The apple node the unregister
     */
    fun unregister(node: AppleNode) {
        thread.ensureMainThread()
        pumpkin.logger.info("Unregistering apple node (nodeId: ${node.nodeIdentifier})")

        node.dockerNode.usedPorts -= node.port
        registeredAppleNodes.remove(node)

        // Send out redis signal
        val signal = AppleNodeUnregisterSignal().apply {
            nodeIdentifier = node.nodeIdentifier
        }

        pumpkin.redis.publish(APPLE_NODE_UNREGISTER, signal).subscribe()
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
            it.nodeIdentifier == nodeId
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
            it.nodeIdentifier == nodeId
        }
    }
}