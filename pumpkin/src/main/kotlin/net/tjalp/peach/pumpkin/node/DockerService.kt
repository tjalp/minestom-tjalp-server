package net.tjalp.peach.pumpkin.node

import net.tjalp.peach.pumpkin.PumpkinServer

class DockerService(
    private val pumpkin: PumpkinServer
) {

    val registeredNodes: Set<DockerNode>
        get() = nodes

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
     * Get an available [DockerNode]
     *
     * @return The docker node
     */
    fun randomDockerNode(): DockerNode {
        if (nodes.isEmpty()) {
            throw IllegalArgumentException("No docker nodes are available")
        }

        return nodes.random()
    }
}