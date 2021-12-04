package net.tjalp.peach.pumpkin

import net.tjalp.peach.peel.config.JsonConfig
import net.tjalp.peach.peel.database.RedisManager
import net.tjalp.peach.peel.exception.FailedOperationException
import net.tjalp.peach.peel.util.generateRandomString
import net.tjalp.peach.pumpkin.config.PumpkinConfig
import net.tjalp.peach.pumpkin.node.DockerService
import net.tjalp.peach.pumpkin.node.Node.Type.*
import net.tjalp.peach.pumpkin.node.NodeService
import net.tjalp.peach.pumpkin.node.RpcService
import net.tjalp.peach.pumpkin.player.PlayerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class PumpkinServer {

    lateinit var logger: Logger; private set

    /** The redis manager service */
    lateinit var redis: RedisManager; private set

    private lateinit var pumpkinConfig: JsonConfig<PumpkinConfig>

    private var initialized: Boolean = false
    private var isRunning: Boolean = false

    lateinit var nodeService: NodeService; private set
    lateinit var rpcService: RpcService; private set
    lateinit var playerService: PlayerService; private set
    lateinit var dockerService: DockerService; private set

    /**
     * The main thread
     */
    val mainThread = PumpkinMainThread.create()

    /** The pumpkin config */
    val config: PumpkinConfig
        get() = pumpkinConfig.data

    /**
     * Initialize the PumpkinServer
     */
    fun init(configFile: File) {
        this.logger = LoggerFactory.getLogger(this::class.java)
        this.pumpkinConfig = JsonConfig(configFile, PumpkinConfig::class.java)

        // Set the initialized state to true
        this.initialized = true

        logger.info("Initialized pumpkin")
    }

    /**
     * Start the PumpkinServer
     */
    fun start() {
        if (!initialized) {
            throw FailedOperationException("PumpkinServer#init must be called before PumpkinServer#start")
        }

        instance = this

        // Initialize various services
        nodeService = NodeService(this)
        val redisDetails = config.redis
        redis = RedisManager(logger, "pumpkin", redisDetails.server, redisDetails.port, redisDetails.password)
        rpcService = RpcService(this)
        playerService = PlayerService(this)
        dockerService = DockerService(this)

        // Initialize services
        nodeService.setup()
        playerService.setup()
        dockerService.setup()

        redis.query().set("velocitySecret", generateRandomString(12)).subscribe()

        rpcService.start()

        isRunning = true

        logger.info("Started PumpkinServer")

        // TEMPORARY FOR DEVELOPMENT
        thread(name = "Console Scanner") {
            while (isRunning) {
                val scanner = Scanner(System.`in`)

                when (scanner.nextLine()) {
                    "stop" -> shutdown()
                    "melon" -> dockerService.randomDockerNode().createNode(
                        type = MELON,
                        port = 25565
                    )
                    "ag" -> dockerService.randomDockerNode().createNode(APPLE_GREEN)
                    "ar" -> dockerService.randomDockerNode().createNode(APPLE_RED)
                }
            }
        }
    }

    /**
     * Shut down the PumpkinServer
     */
    fun shutdown() {
        if (!isRunning) return

        logger.info("Shutting down services")

        try {
            mainThread.syncTask {
                redis.dispose()
                rpcService.stop()

                isRunning = false
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        finally {
            mainThread.shutdown()
        }
    }

    companion object {

        private lateinit var instance: PumpkinServer

        fun get(): PumpkinServer {
            return instance
        }
    }
}