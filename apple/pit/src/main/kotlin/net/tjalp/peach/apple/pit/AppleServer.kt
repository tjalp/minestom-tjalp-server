package net.tjalp.peach.apple.pit

import net.tjalp.peach.apple.pit.config.AppleConfig
import net.tjalp.peach.peel.config.JsonConfig
import net.tjalp.peach.peel.database.RedisManager
import org.slf4j.Logger

abstract class AppleServer {

    /**
     * The slf4j logger every server should
     * use.
     */
    abstract val logger: Logger

    /**
     * The redis connection every server should
     * have.
     */
    abstract val redis: RedisManager

    abstract val appleConfig: JsonConfig<out AppleConfig>

    /**
     * The apple config that should be used.
     * This is present in every platform
     */
    val config: AppleConfig
        get() = appleConfig.data

    /**
     * Initialize the implementation. This should
     * be called whenever the implementation is
     * supposed to load.
     */
    abstract fun start()

    /**
     * Initiate the shut down sequence of the
     * implementation. This should be called
     * whenever the implementation is supposed
     * to shut down.
     */
    abstract fun shutdown()
}