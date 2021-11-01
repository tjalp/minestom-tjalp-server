package net.tjalp.minestom.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(
        id = "tjalp",
        name = "tjalp proxy",
        description = "The proxy plugin needed for tjalp's network",
        version = "0.1.0",
        url = "https://tjalp.net/",
        authors = {"tjalp"}
)
public class Proxy {

    @Inject
    private ProxyServer proxy;

    @Inject
    private Logger logger;

    /**
     * Gets the proxy
     *
     * @return the proxy
     */
    public ProxyServer proxy() {
        return this.proxy;
    }

    /**
     * Gets the logger
     *
     * @return the logger
     */
    public Logger logger() {
        return this.logger;
    }

    /*
     *
     * ----- LOGIC STARTS HERE -----
     *
     */


    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Hello world!");
    }
}
