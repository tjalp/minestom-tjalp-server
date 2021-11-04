package net.tjalp.peach.apple.pit;

import net.tjalp.peach.peel.database.RedisManager;

/**
 * Every implementation should be using this
 * interface.
 */
public interface AppleServer {

    /**
     * Initialize the implementation. This should
     * be called whenever the implementation is
     * supposed to load.
     */
    void start();

    /**
     * Initiate the shut down sequence of the
     * implementation. This should be called
     * whenever the implementation is supposed
     * to shut down.
     */
    void shutdown();

    /**
     * Get the [RedisManager] of this
     * implementation
     *
     * @return the Redis Manager
     */
    RedisManager redis();
}
