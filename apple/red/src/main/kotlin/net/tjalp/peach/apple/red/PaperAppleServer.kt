package net.tjalp.peach.apple.red

import net.tjalp.peach.apple.pit.AppleServer
import net.tjalp.peach.apple.red.config.PaperAppleConfig
import net.tjalp.peach.apple.red.listener.AppleEventListener
import net.tjalp.peach.peel.config.JsonConfig
import net.tjalp.peach.peel.util.GsonHelper
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.charset.StandardCharsets

class PaperAppleServer : AppleServer() {

    lateinit var plugin: Plugin

    override fun init() {
        super.init()

        // Override the apple config because there will be some custom values in here
        config = GsonHelper.global().fromJson(System.getenv("NODE_CONFIG"), PaperAppleConfig::class.java)

        logger = plugin.slF4JLogger
    }

    override fun start() {
        super.start()

        // Enable the proxy (must be done after redis has connected)
        redis.query().get("velocitySecret").subscribe { secret ->
            val clazz = Class.forName("com.destroystokyo.paper.PaperConfig")
            val field = clazz.getDeclaredField("velocitySecretKey")
            field.isAccessible = true
            field.set(null, secret.toByteArray(StandardCharsets.UTF_8))
        }

        AppleEventListener(this)
    }

    /**
     * The plugin that manages the initialization,
     * start and shutdown of an [AppleServer].
     */
    class Plugin : JavaPlugin() {

        lateinit var server: PaperAppleServer

        override fun onEnable() {
            server = PaperAppleServer()

            // Initialize the Paper server
            server.plugin = this
            server.init()
            server.start()
        }

        override fun onDisable() {
            server.shutdown()
        }
    }
}