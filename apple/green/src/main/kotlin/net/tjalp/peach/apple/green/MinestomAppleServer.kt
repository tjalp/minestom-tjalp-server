package net.tjalp.peach.apple.green

import net.minestom.server.MinecraftServer
import net.minestom.server.extras.bungee.BungeeCordProxy
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.Instance
import net.tjalp.peach.apple.green.command.SkinCommand
import net.tjalp.peach.apple.green.command.StopCommand
import net.tjalp.peach.apple.green.command.TeleportCommand
import net.tjalp.peach.apple.green.config.MinestomAppleConfig
import net.tjalp.peach.apple.green.generator.SimpleGenerator
import net.tjalp.peach.apple.green.listener.AppleEventListener
import net.tjalp.peach.apple.green.old.command.GamemodeCommand
import net.tjalp.peach.apple.green.registry.OVERWORLD
import net.tjalp.peach.apple.green.registry.registerBiomes
import net.tjalp.peach.apple.green.registry.registerDimensions
import net.tjalp.peach.apple.pit.AppleServer
import net.tjalp.peach.peel.config.JsonConfig
import net.tjalp.peach.peel.database.RedisManager
import org.slf4j.Logger
import java.io.File

fun main(args: Array<String>) {
    val server = MinestomAppleServer()

    // Initialize the Minestom server
    server.start()
}

class MinestomAppleServer : AppleServer() {

    override lateinit var logger: Logger; private set
    override lateinit var redis: RedisManager; private set
    override lateinit var appleConfig: JsonConfig<MinestomAppleConfig>; private set

    /** The main instance which is loaded at all times */
    lateinit var overworld: Instance

    override fun start() {
        instance = this
        appleConfig = JsonConfig(File("config.json"), MinestomAppleConfig::class.java)

        // Initialize the Minestom server
        val server = MinecraftServer.init()
        val instanceManager = MinecraftServer.getInstanceManager()

        // Set the logger
        logger = MinecraftServer.LOGGER

        // Initialize various services
        val redisDetails = config.redis
        redis = RedisManager(
            logger,
            "apple", // TODO fix nodeIds
            redisDetails.server,
            redisDetails.port,
            redisDetails.password
        )

        // Enable Mojang authentication (disabled because we're using a proxy, see below for Velocity)
        // MojangAuth.init();

        // Enable the proxy
        val velocitySecret = redis.query().get("velocitySecret").block()
        if (velocitySecret != null) VelocityProxy.enable(velocitySecret) else BungeeCordProxy.enable()

        // Register the main listener
        AppleEventListener(this)

        // Register the services
        registerCommands()
        registerDimensions()
        registerBiomes()

        // Set some Minestom properties
        // System.setProperty("minestom.chunk-view-distance", "8")
        MinecraftServer.setBrandName("apple")

        // Create the instance
        overworld = instanceManager.createInstanceContainer(OVERWORLD)
        overworld.chunkGenerator = SimpleGenerator()

        // Specify a shutdown task
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::shutdown).schedule()

        // Start the server
        server.start("0.0.0.0", 25000)
    }

    override fun shutdown() {
        logger.info("Shutting down services")

        redis.dispose()
    }

    /**
     * Register the commands the server should use
     */
    private fun registerCommands() {
        val man = MinecraftServer.getCommandManager()

        man.register(GamemodeCommand())
        man.register(SkinCommand())
        man.register(StopCommand())
        man.register(TeleportCommand())
    }

    companion object {

        /** The server instance, there should only be one */
        private lateinit var instance: MinestomAppleServer

        /**
         * Get the server that is currently running
         *
         * @return the server that is currently running
         */
        fun get(): MinestomAppleServer {
            return instance
        }
    }
}