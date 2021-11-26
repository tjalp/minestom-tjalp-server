package net.tjalp.peach.apple.pit

import io.grpc.ManagedChannel
import net.tjalp.peach.apple.pit.config.AppleConfig
import net.tjalp.peach.peel.config.JsonConfig
import net.tjalp.peach.peel.database.RedisManager
import net.tjalp.peach.peel.network.HealthReporter
import net.tjalp.peach.peel.network.PeachRPC
import net.tjalp.peach.peel.util.generateRandomString
import net.tjalp.peach.proto.apple.Apple
import net.tjalp.peach.proto.apple.AppleServiceGrpcKt.AppleServiceCoroutineStub
import org.slf4j.Logger

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

    lateinit var appleConfig: JsonConfig<out AppleConfig>

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
    val config: AppleConfig
        get() = appleConfig.data

    /**
     * The current apple node's identifier
     */
    val nodeIdentifier: String = "a-${generateRandomString(6)}"

    /**
     * Initialize the implementation. Must be called
     * before [AppleServer.start]
     */
    open fun init() {

    }

    /**
     * Start the implementation. This should
     * be called whenever the implementation is
     * supposed to load. This method requires
     * [AppleServer.start] to be called before
     * this
     */
    open fun start() {
        instance = this

        // Initialize RPC
        rpcChannel = PeachRPC.createChannel(
            nodeId = nodeIdentifier,
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
        healthReporter.connect() // TODO Connect on redis signal from pumpkin
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
    }

    /**
     * Send the proxy handshake to pumpkin
     * to register the current melon node
     */
    internal suspend fun sendAppleHandshake() {
        val request = Apple.AppleHandshakeRequest.newBuilder()
            .setNodeIdentifier(nodeIdentifier)
            .setPort(config.port)

        logger.info("Sending apple handshake")
        val response = rpcStub.appleHandshake(request.build())
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