package net.tjalp.peach.apple.red

import net.tjalp.peach.apple.pit.AppleServer
import net.tjalp.peach.apple.red.config.PaperAppleConfig
import net.tjalp.peach.peel.config.JsonConfig
import net.tjalp.peach.peel.database.RedisManager
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import java.io.File

class PaperAppleServer : AppleServer() {

    class Plugin : JavaPlugin() {

        lateinit var server: PaperAppleServer

        override fun onEnable() {
            plugin = this
            server = PaperAppleServer()

            // Initialize the Paper server
            server.start()
        }

        override fun onDisable() {
            server.shutdown()
        }

        companion object {

            private lateinit var plugin: Plugin

            fun get(): Plugin {
                return plugin
            }
        }
    }

    override lateinit var logger: Logger; private set
    override lateinit var redis: RedisManager; private set
    override lateinit var appleConfig: JsonConfig<PaperAppleConfig>; private set

    val plugin = Plugin.get()

    override fun start() {
        instance = this
        appleConfig = JsonConfig(File("config.json"), PaperAppleConfig::class.java)
        logger = plugin.slF4JLogger

        // Initialize various services
        val redisDetails = config.redis
        redis = RedisManager(
            logger,
            "apple", // TODO fix nodeIds
            redisDetails.server,
            redisDetails.port,
            redisDetails.password
        )
    }

    override fun shutdown() {
        logger.info("Shutting down services")

        redis.dispose()
    }

    companion object {

        /** The server instance, there should only be one */
        private lateinit var instance: PaperAppleServer

        /**
         * Get the server that is currently running
         *
         * @return the server that is currently running
         */
        fun get(): PaperAppleServer {
            return instance
        }
    }
}