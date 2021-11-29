package net.tjalp.peach.pumpkin.node

import net.tjalp.peach.pumpkin.DockerNode
import net.tjalp.peach.pumpkin.PumpkinServer

class DockerService(
    private val pumpkin: PumpkinServer
) {

    private val nodes = HashSet<DockerNode>()

    /**
     * Set up the docker service
     */
    fun setup() {
        for (details in pumpkin.config.dockerNodes) {
            nodes += DockerNode(details)
        }
    }

    /**
     * Create a [Node]
     *
     * @param type The node type
     */
    fun createNode(type: Node.Type, dockerNode: DockerNode? = null) {
        if (dockerNode == null && nodes.isEmpty()) {
            pumpkin.logger.error("Tried to create a node, but there are no available docker nodes")
            return
        }

        val node = dockerNode ?: nodes.random()

        node.createNode(type)
    }
}