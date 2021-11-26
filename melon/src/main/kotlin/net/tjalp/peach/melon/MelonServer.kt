package net.tjalp.peach.melon

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerInfo
import io.grpc.ManagedChannel
import net.tjalp.peach.melon.config.MelonConfig
import net.tjalp.peach.melon.listener.MelonEventListener
import net.tjalp.peach.melon.listener.MelonSignalListener
import net.tjalp.peach.peel.config.JsonConfig
import net.tjalp.peach.peel.database.RedisManager
import net.tjalp.peach.peel.network.HealthReporter
import net.tjalp.peach.peel.network.PeachRPC
import net.tjalp.peach.peel.util.generateRandomString
import net.tjalp.peach.proto.melon.Melon.MelonHandshakeRequest
import net.tjalp.peach.proto.melon.Melon.MelonHealthReport
import net.tjalp.peach.proto.melon.MelonServiceGrpcKt.MelonServiceCoroutineStub
import org.slf4j.Logger
import java.io.File
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.*

@Plugin(
    id = "melon",
    name = "Melon",
    description = "The proxy plugin needed for tjalp's network",
    version = "0.1.0",
    url = "https://tjalp.net/",
    authors = ["tjalp"]
)
class MelonServer {

    /**
     * The proxy server which the MelonServer runs on
     */
    @Inject
    lateinit var proxy: ProxyServer

    /**
     * The logger to use
     */
    @Inject
    lateinit var logger: Logger

    lateinit var melonConfig: JsonConfig<MelonConfig>; private set

    /**
     * The client RPC channel
     */
    lateinit var rpcChannel: ManagedChannel; private set
    lateinit var rpcStub: MelonServiceCoroutineStub; private set

    /**
     * The heartbeat
     */
    lateinit var healthReporter: HealthReporter<MelonHealthReport>; private set

    /**
     * The redis manager
     */
    lateinit var redis: RedisManager; private set

    /**
     * The current node identifier
     */
    val nodeIdentifier: String = "m-${generateRandomString(6)}"

    /**
     * The melon config
     */
    val config: MelonConfig
        get() = melonConfig.data

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        melonConfig = JsonConfig(File("config.json"), MelonConfig::class.java)

        rpcChannel = PeachRPC.createChannel(
            nodeIdentifier,
            logger = logger,
            config = config.pumpkin
        ).build()

        rpcStub = MelonServiceCoroutineStub(rpcChannel)

        // Initialize various services
        val redisDetails = config.redis

        healthReporter = MelonHealthReporter(this)
        redis = RedisManager(
            logger,
            nodeIdentifier,
            redisDetails.server,
            redisDetails.port,
            redisDetails.password
        )

        // Set the secret
        setVelocitySecret()

        healthReporter.start()

        // TODO Connect when a redis signal from pumpkin is received
        healthReporter.connect()

        // Register listeners
        MelonEventListener(this)
        MelonSignalListener(this)

        logger.info("Registered listeners")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        healthReporter.stop()
        rpcChannel.shutdownNow()
    }

    /**
     * Register an apple node
     *
     * @param id the server id to use
     * @param address the target server address
     * @param port the target server port
     */
    fun registerAppleNode(id: String, address: String, port: Int) {
        val inet = InetSocketAddress(address, port)
        proxy.registerServer(ServerInfo(id, inet))
    }

    /**
     * Unregister an apple node
     */
    fun unregisterAppleNode(id: String) {
        val server = proxy.getServer(id)

        if (server.isEmpty) return

        proxy.unregisterServer(server.get().serverInfo)
    }

    /**
     * Send the proxy handshake to pumpkin
     * to register the current melon node
     */
    internal suspend fun sendMelonHandshake() {
        val request = MelonHandshakeRequest.newBuilder()
            .setNodeIdentifier(nodeIdentifier)

        logger.info("Sending proxy handshake")
        val response = rpcStub.melonHandshake(request.build())
        logger.info("Registering servers")

        response.appleNodeRegistrationList.forEach {
            logger.info("Registering server (nodeId: ${it.nodeId})")
            registerAppleNode(it.nodeId, it.server, it.port)
        }
    }

    /**
     * Set the Velocity secret from redis
     */
    private fun setVelocitySecret() {
        redis.query().get("velocitySecret").subscribe { secret ->
            val clazz = Class.forName("com.velocitypowered.proxy.config.VelocityConfiguration")
            val field = clazz.getDeclaredField("forwardingSecret")
            field.isAccessible = true
            field.set(proxy.configuration, secret.toByteArray(StandardCharsets.UTF_8))
        }
    }
}