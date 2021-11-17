package net.tjalp.peach.apple.red

import net.tjalp.peach.apple.pit.AppleServer
import net.tjalp.peach.apple.red.config.PaperAppleConfig
import net.tjalp.peach.apple.red.listener.AppleEventListener
import net.tjalp.peach.peel.config.JsonConfig
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PaperAppleServer : AppleServer() {

    lateinit var plugin: Plugin

    override fun init() {
        super.init()

        appleConfig = JsonConfig(File("config.json"), PaperAppleConfig::class.java)
        logger = plugin.slF4JLogger
    }

    override fun start() {
        super.start()

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