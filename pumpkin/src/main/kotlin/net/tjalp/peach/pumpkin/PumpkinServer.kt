package net.tjalp.peach.pumpkin

import net.tjalp.peach.peel.config.JsonConfig
import net.tjalp.peach.peel.database.RedisManager
import net.tjalp.peach.peel.exception.FailedOperationException
import net.tjalp.peach.pumpkin.config.PumpkinConfig
import net.tjalp.peach.pumpkin.node.RpcService
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

    lateinit var rpcService: RpcService; private set

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

        // Initialize various services
        val redisDetails = config.redis
        redis = RedisManager(logger, "pumpkin", redisDetails.server, redisDetails.port, redisDetails.password)
        rpcService = RpcService(this)

        // TODO Set random Velocity secret
        redis.transactionLegacy {
            set("velocitySecret", "OpkUJU3FGM3I").subscribe()
        }.subscribe()

        rpcService.start()

        isRunning = true

        logger.info("Started PumpkinServer")

        // TEMPORARY FOR DEVELOPMENT
        thread(name = "Console Scanner") {
            while (isRunning) {
                val scanner = Scanner(System.`in`)

                if (scanner.nextLine() == "stop") {
                    shutdown()
                }
            }
        }
    }

    /**
     * Shut down the PumpkinServer
     */
    fun shutdown() {
        logger.info("Shutting down services")

        redis.dispose()
        rpcService.stop()

        isRunning = false
    }

    companion object {

        private lateinit var instance: PumpkinServer

        fun get(): PumpkinServer {
            return instance
        }
    }
}