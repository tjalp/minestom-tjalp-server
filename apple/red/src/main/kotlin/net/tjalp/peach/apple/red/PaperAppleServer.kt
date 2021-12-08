package net.tjalp.peach.apple.red

import com.destroystokyo.paper.PaperConfig
import net.tjalp.peach.apple.pit.AppleServer
import net.tjalp.peach.apple.pit.scheduler.AppleScheduler
import net.tjalp.peach.apple.pit.scheduler.ReactiveScheduler
import net.tjalp.peach.apple.red.command.PeachCommand
import net.tjalp.peach.apple.red.config.PaperAppleConfig
import net.tjalp.peach.apple.red.listener.AppleEventListener
import net.tjalp.peach.apple.red.scheduler.PaperAppleScheduler
import net.tjalp.peach.peel.util.GsonHelper
import org.bukkit.plugin.java.JavaPlugin
import java.nio.charset.StandardCharsets

class PaperAppleServer : AppleServer() {

    private lateinit var globalScheduler: PaperAppleScheduler
    lateinit var plugin: Plugin

    override val scheduler: AppleScheduler
        get() = globalScheduler

    override fun init() {
        super.init()

        // Override the apple config because there will be some custom values in here
        config = GsonHelper.global().fromJson(System.getenv("NODE_CONFIG"), PaperAppleConfig::class.java)

        logger = plugin.slF4JLogger

        // Initialize the scheduler
        globalScheduler = PaperAppleScheduler(this)
    }

    override fun start() {
        super.start()

        // Set the secret
        setVelocitySecret()

        // Register listeners
        AppleEventListener(this)

        // Initialize services
        registerCommands()
    }

    /**
     * Set Velocity secret
     */
    private fun setVelocitySecret() {
        redis.query().get("velocitySecret").subscribe { secret ->
            PaperConfig.velocitySecretKey = secret.toByteArray(StandardCharsets.UTF_8)
        }
    }

    /**
     * Register all commands
     */
    private fun registerCommands() {
        PeachCommand(this)
    }

    /**
     * The plugin that manages the initialization,
     * start and shutdown of an [AppleServer].
     */
    class Plugin : JavaPlugin() {

        private lateinit var appleServer: PaperAppleServer

        override fun onEnable() {
            appleServer = PaperAppleServer()

            // Initialize the Paper server
            appleServer.plugin = this
            appleServer.init()
            appleServer.start()
        }

        override fun onDisable() {
            appleServer.shutdown()
        }
    }
}