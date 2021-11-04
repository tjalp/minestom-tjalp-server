package net.tjalp.peach.apple.red;

import net.tjalp.peach.apple.pit.AppleServer;
import net.tjalp.peach.peel.database.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperAppleServer extends JavaPlugin implements AppleServer {

    /** The static plugin instance, there should only be one */
    private static PaperAppleServer instance;

    /**
     * Get the plugin that is currently running
     *
     * @return the plugin that is currently running
     */
    public static PaperAppleServer get() {
        return PaperAppleServer.instance;
    }

    /** The redis manager service */
    private RedisManager redis;

    @Override
    public void onEnable() {
        this.start();
    }

    @Override
    public void onDisable() {
        this.shutdown();
    }

    @Override
    public void start() {
        instance = this;

        // Initialize various services
        this.redis = new RedisManager(getSLF4JLogger());
    }

    @Override
    public void shutdown() {
        redis().dispose();
    }

    public RedisManager redis() {
        return this.redis;
    }
}
