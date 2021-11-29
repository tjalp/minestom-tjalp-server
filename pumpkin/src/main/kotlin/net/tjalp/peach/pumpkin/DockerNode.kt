package net.tjalp.peach.pumpkin

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.github.dockerjava.transport.DockerHttpClient
import net.tjalp.peach.peel.config.DockerDetails
import net.tjalp.peach.peel.util.generateRandomString
import net.tjalp.peach.pumpkin.node.Node
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
        nodeId: String = "${type.shortName}-${generateRandomString(6)}",
        memory: Long = 512L,
        maxCpuPercent: Long = 50
    ) {
        val hostConfig = HostConfig.newHostConfig()
            .withMemory(memory * 1_000_000)
            .withCpuPercent(maxCpuPercent)

        client.createContainerCmd(type.fullName)
            .withName(nodeId)
            .withHostConfig(hostConfig)
            .withEnv("NODE_ID=$nodeId")
            .exec()
    }

}