package net.tjalp.peach.apple.pit

import io.grpc.ManagedChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.tjalp.peach.apple.pit.config.AppleConfig
import net.tjalp.peach.apple.pit.listener.AppleSignalListener
import net.tjalp.peach.apple.pit.scheduler.AppleScheduler
import net.tjalp.peach.apple.pit.scheduler.ReactiveScheduler
import net.tjalp.peach.peel.database.RedisManager
import net.tjalp.peach.peel.network.HealthReporter
import net.tjalp.peach.peel.network.PeachRPC
import net.tjalp.peach.proto.apple.Apple
import net.tjalp.peach.proto.apple.AppleServiceGrpcKt.AppleServiceCoroutineStub
import org.slf4j.Logger
import java.util.*

/**
 * This abstract class contains all the common
 * logic every AppleServer implementation
 * should have.
 */
abstract class AppleServer {

    /**
     * The slf4j logger every server should
     * use.
     */
    lateinit var logger: Logger

    /**
     * The RPC management
     */
    lateinit var rpcChannel: ManagedChannel; private set
    lateinit var rpcStub: AppleServiceCoroutineStub

    /**
     * The heartbeat
     */
    lateinit var healthReporter: HealthReporter<Apple.AppleHealthReport>; private set

    /**
     * The redis connection every server should
     * have.
     */
    lateinit var redis: RedisManager
        private set

    /**
     * The apple config that should be used.
     * This is present in every platform
     */
    lateinit var config: AppleConfig

    /**
     * The global scheduler
     */
    abstract val scheduler: AppleScheduler<out ReactiveScheduler>

    /**
     * Whether the [AppleServer] has been
     * initialized.
     */
    private var initialized = false

    /**
     * The current apple node's identifier
     */
    val nodeId: String
        get() = config.nodeId

    /**
     * Initialize the implementation. Must be called
     * before [AppleServer.start]
     */
    open fun init() {
        this.initialized = true
    }

    /**
     * Start the implementation. This should
     * be called whenever the implementation is
     * supposed to load. This method requires
     * [AppleServer.start] to be called before
     * this
     */
    open fun start() {
        if (!initialized) {
            throw IllegalStateException("AppleServer#init must be called before AppleServer#start")
        }

        instance = this

        // Initialize RPC
        rpcChannel = PeachRPC.createChannel(
            nodeId = nodeId,
            logger = logger,
            config = config.pumpkin
        ).build()

        rpcStub = AppleServiceCoroutineStub(rpcChannel)

        // Initialize various services
        val redisDetails = config.redis

        healthReporter = AppleHealthReporter(this)
        redis = RedisManager(
            logger,
            "apple",
            redisDetails.server,
            redisDetails.port,
            redisDetails.password
        )

        healthReporter.start()

        // Register listeners
        AppleSignalListener(this)
    }

    /**
     * Initiate the shut down sequence of the
     * implementation. This should be called
     * whenever the implementation is supposed
     * to shut down.
     */
    open fun shutdown() {
        logger.info("Shutting down AppleServer")

        healthReporter.stop()
        redis.dispose()
        scheduler.cancel()
    }

    /**
     * Send the proxy handshake to pumpkin
     * to register the current melon node
     */
    internal suspend fun sendAppleHandshake() {
        val request = Apple.AppleHandshakeRequest.newBuilder()
            .setNodeIdentifier(nodeId)
            .setPort(config.port)

        logger.info("Sending apple handshake")
        val response = rpcStub.appleHandshake(request.build())
    }

    /**
     * Send the current player to another apple node
     * TODO Use this an extension on a profile
     *
     * @param uniqueId The player's unique identifier
     * @param nodeId The target node's unique identifier
     */
    fun switchPlayer(uniqueId: UUID, nodeId: String) {
        GlobalScope.launch {
            val request = Apple.PlayerSwitchRequest.newBuilder()
                .setPlayerUniqueIdentifier(uniqueId.toString())
                .setAppleNodeIdentifier(nodeId)
                .build()

            val response = rpcStub.playerSwitch(request)

            if (!response.success) {
                logger.info("Failed to switch player")
            }
        }
    }

    companion object {

        /** The server instance, there should only be one */
        private lateinit var instance: AppleServer

        /**
         * Get the server that is currently running
         *
         * @return the server that is currently running
         */
        fun get(): AppleServer {
            return instance
        }
    }
}