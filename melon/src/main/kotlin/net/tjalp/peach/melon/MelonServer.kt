package net.tjalp.peach.melon

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerInfo
import net.tjalp.peach.melon.listener.MelonEventListener
import org.slf4j.Logger
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

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {

        // Register listeners
        proxy.eventManager.register(this, MelonEventListener(this))

        logger.info("Registered listeners")

        // Register active servers
        registerServer("apple", "host.docker.internal", 25000)

        logger.info("Registered active servers")
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
}