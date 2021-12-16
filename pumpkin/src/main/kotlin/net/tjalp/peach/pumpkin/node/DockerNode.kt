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
import kotlin.random.Random
import kotlin.random.nextInt

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
    internal val usedPorts = HashSet<Int>()

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
        config: NodeConfig? = null,
        nodeId: String? = null,
        port: Int? = null,
        memory: Long? = null,
        maxCpuPercent: Long? = null
    ): UnregisteredNode {
        val defConfig = config ?: NodeConfig()
        val defNodeId = nodeId ?: "${type.shortName}-${generateRandomString(6)}"
        val defPort = port ?: Random.nextInt(IntRange(25000, 25999))
        // TODO DEVELOPMENT THIS SHOULD BE BETTER
        val defMemory = memory ?: if (type == Node.Type.APPLE_RED) 2048L else 512L

        if (defPort in usedPorts) {
            throw IllegalArgumentException("Port $defPort is not available on this docker node (${this.config.dockerHost.host})")
        }

        val exposedPort = ExposedPort.tcp(defPort)
        val ports = Ports().apply {
            bind(exposedPort, Ports.Binding.bindPort(defPort))
        }
        val hostConfig = HostConfig.newHostConfig()
            .withMemory(defMemory * 1_000_000)
            .withMemoryReservation(defMemory * 1_000_000)
            .withPortBindings(ports)
            .withAutoRemove(true)
            .withExtraHosts("host.docker.internal:host-gateway")
            .withNetworkMode("host") // TODO Not make this host

        // Set cpu percentage if applicable
        if (maxCpuPercent != null) hostConfig.withCpuPercent(maxCpuPercent)

        // Set the config properties
        defConfig.nodeId = defNodeId
        defConfig.port = defPort

        pumpkin.mainThread.asyncTask {
            client.createContainerCmd(type.imageName)
                .withName(defNodeId)
                .withExposedPorts(exposedPort)
                .withHostConfig(hostConfig)
                .withEnv("NODE_CONFIG=${GsonHelper.global().toJson(defConfig)}", "PORT=$defPort")
                .exec()
            client.startContainerCmd(defNodeId).exec()
        }

        return UnregisteredNode(defNodeId, type)
    }

    /**
     * Stop a [Node]
     *
     * @param nodeId The target node's identifier
     */
    fun stopNode(nodeId: String) {
        pumpkin.mainThread.asyncTask {
            client.stopContainerCmd(nodeId)
                .withTimeout(15)
                .exec()
        }
    }

    /**
     * See [stopNode]
     *
     * @param node The target node
     */
    fun stopNode(node: Node) {
        stopNode(node.nodeIdentifier)
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
     * @param node The target node
     */
    fun killNode(node: Node) {
        killNode(node.nodeIdentifier)
    }

}