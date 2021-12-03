package net.tjalp.peach.pumpkin.node

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Ports
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.github.dockerjava.transport.DockerHttpClient
import net.tjalp.peach.peel.config.DockerDetails
import net.tjalp.peach.peel.config.NodeConfig
import net.tjalp.peach.peel.util.GsonHelper
import net.tjalp.peach.peel.util.generateRandomString
import net.tjalp.peach.pumpkin.PumpkinServer
import java.time.Duration

class DockerNode(
    val details: DockerDetails
) {

    private val pumpkin = PumpkinServer.get()

    /**
     * The [DockerClientConfig] that is used for this docker node
     */
    private val config: DockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost("tcp://${details.server}:${details.port}")
        .build()

    /**
     * The [DockerHttpClient] that is used for this docker node
     */
    private val httpClient: DockerHttpClient = ApacheDockerHttpClient.Builder()
        .dockerHost(config.dockerHost)
        .sslConfig(config.sslConfig)
        .maxConnections(details.maxConnections)
        .connectionTimeout(Duration.ofSeconds(15))
        .responseTimeout(Duration.ofSeconds(20))
        .build()

    /**
     * The [DockerClient] that is created from the [config] & [httpClient]
     */
    val client: DockerClient = DockerClientImpl.getInstance(config, httpClient)

    /**
     * The available ports on this docker node
     */
    private val availablePorts = HashSet<Int>().apply {
        repeat(1000) {
            add(it + 25000)
        }
    }

    /**
     * Create a [Node] on this [DockerNode]
     *
     * @param type The node type
     * @param nodeId The node id to use for this node
     * @param memory The memory in MB
     * @param maxCpuPercent The maximum percentage of cpu this node may use. 100% is equal to one core
     */
    fun createNode(
        type: Node.Type,
        config: NodeConfig = NodeConfig(),
        nodeId: String = "${type.shortName}-${generateRandomString(6)}",
        port: Int = availablePorts.random(),
        memory: Long = if (type == Node.Type.APPLE_RED) 2048L else 512L, // TODO DEVELOPMENT THIS SHOULD BE BETTER
        maxCpuPercent: Long? = null
    ): UnregisteredNode {
        if (port !in availablePorts) {
            throw IllegalArgumentException("Port $port is not available on this docker node (${this.config.dockerHost.host})")
        }

        val exposedPort = ExposedPort.tcp(port)
        val ports = Ports().apply {
            bind(exposedPort, Ports.Binding.bindPort(port))
        }
        val hostConfig = HostConfig.newHostConfig()
            .withMemory(memory * 1_000_000)
            .withMemoryReservation(memory * 1_000_000)
            .withPortBindings(ports)
            .withAutoRemove(true)
            .withExtraHosts("host.docker.internal:host-gateway")
            .withNetworkMode("host") // TODO Not make this host

        // Set cpu percentage if applicable
        if (maxCpuPercent != null) hostConfig.withCpuPercent(maxCpuPercent)

        // Remove the port from the available ports of this docker node
        availablePorts.remove(port)

        // Set the config properties
        config.nodeId = nodeId
        config.port = port

        pumpkin.mainThread.asyncTask {
            client.createContainerCmd(type.imageName)
                .withName(nodeId)
                .withExposedPorts(exposedPort)
                .withHostConfig(hostConfig)
                .withEnv("NODE_CONFIG=${GsonHelper.global().toJson(config)}", "PORT=$port")
                .exec()
            client.startContainerCmd(nodeId).exec()
        }

        return UnregisteredNode(nodeId, type)
    }

    /**
     * Kill a [Node]
     *
     * @param nodeId The target node's identifier
     */
    fun killNode(nodeId: String) {
        pumpkin.mainThread.asyncTask {
            client.killContainerCmd(nodeId).exec()
        }
    }

    /**
     * See [killNode]
     *
     * @param [node] The target node
     */
    fun killNode(node: Node) {
        killNode(node.nodeIdentifier)
    }

}