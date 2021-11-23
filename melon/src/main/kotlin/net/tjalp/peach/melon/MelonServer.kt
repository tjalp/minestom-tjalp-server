package net.tjalp.peach.melon

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerInfo
import io.grpc.ManagedChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.tjalp.peach.melon.config.MelonConfig
import net.tjalp.peach.melon.listener.MelonEventListener
import net.tjalp.peach.peel.config.JsonConfig
import net.tjalp.peach.peel.database.RedisManager
import net.tjalp.peach.peel.network.PeachRPC
import net.tjalp.peach.proto.melon.Melon
import net.tjalp.peach.proto.melon.MelonServiceGrpcKt.MelonServiceCoroutineStub
import org.slf4j.Logger
import java.io.File
import java.net.InetSocketAddress

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
     * The redis manager
     */
    lateinit var redis: RedisManager; private set

    /**
     * The melon config
     */
    val config: MelonConfig
        get() = melonConfig.data

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        melonConfig = JsonConfig(File("config.json"), MelonConfig::class.java)

        rpcChannel = PeachRPC.createChannel(
            logger = logger,
            config = config.pumpkin
        ).build()

        rpcStub = MelonServiceCoroutineStub(rpcChannel)

        // Initialize various services
        val redisDetails = config.redis
        redis = RedisManager(
            logger,
            "melon", // TODO fix nodeIds
            redisDetails.server,
            redisDetails.port,
            redisDetails.password
        )

        // Send the proxy handshake
        sendProxyHandshake()

        // Register listeners
        proxy.eventManager.register(this, MelonEventListener(this))

        logger.info("Registered listeners")
    }

    /**
     * Register a server globally (on all proxies)
     *
     * @param id the server id to use
     * @param address the target server address
     * @param port the target server port
     */
    private fun registerServer(id: String, address: String, port: Int) {
        val inet = InetSocketAddress(address, port)
        proxy.registerServer(ServerInfo(id, inet))
    }

    private fun sendProxyHandshake() {
        val request = Melon.ProxyHandshakeRequest.newBuilder()

        GlobalScope.async {
            logger.info("Sending proxy handshake")
            val response = rpcStub.proxyHandshake(request.build())
            logger.info("Registering servers")

            response.appleNodeRegistrationList.forEach {
                logger.info("Registering server (nodeId: ${it.nodeId})")
                registerServer(it.nodeId, it.server, it.port)
            }
        }
    }
}