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
import java.time.Duration

class DockerNode(
    private val details: DockerDetails
) {

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

    val availablePorts = HashSet<Int>().apply {
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
        config: NodeConfig,
        nodeId: String = "${type.shortName}-${generateRandomString(6)}",
        port: Int = availablePorts.random(),
        memory: Long = 512L,
        maxCpuPercent: Long = 50
    ) {
        val exposedPort = ExposedPort.tcp(port)
        val ports = Ports().apply {
            bind(exposedPort, Ports.Binding.bindPort(port))
        }
        val hostConfig = HostConfig.newHostConfig()
            .withMemory(memory * 1_000_000)
            .withCpuPercent(maxCpuPercent)
            .withPortBindings(ports)
            .withAutoRemove(true)
            .withExtraHosts("host.docker.internal:host-gateway")

        // Remove the port from the available ports of this docker node
        availablePorts.remove(port)

        // Set the config properties
        config.nodeId = nodeId
        config.port = port

        client.createContainerCmd(type.imageName)
            .withName(nodeId)
            .withExposedPorts(exposedPort)
            .withHostConfig(hostConfig)
            .withEnv("NODE_CONFIG=${GsonHelper.global().toJson(config)}")
            .exec()
        client.startContainerCmd(nodeId).exec()
    }

}