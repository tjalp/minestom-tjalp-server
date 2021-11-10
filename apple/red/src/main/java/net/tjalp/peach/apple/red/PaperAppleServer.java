package net.tjalp.peach.apple.red;

import net.tjalp.peach.apple.pit.AppleServer;
import net.tjalp.peach.apple.pit.config.AppleConfig;
import net.tjalp.peach.peel.config.JsonConfig;
import net.tjalp.peach.peel.database.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class PaperAppleServer extends JavaPlugin {

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

    public void start() {
        instance = this;

        // Initialize various services
        this.redis = new RedisManager(getSLF4JLogger(), "apple", "localhost", 6379, "");
    }

    public void shutdown() {
        redis.dispose();
    }

    public @NotNull RedisManager getRedis() {
        return this.redis;
    }

    @NotNull
    public JsonConfig<AppleConfig> getAppleConfig() {
        return null;
    }

    class Test extends AppleServer {

        @NotNull
        @Override
        public Logger getLogger() {
            return null;
        }

        @NotNull
        @Override
        public RedisManager getRedis() {
            return null;
        }

        @NotNull
        @Override
        public JsonConfig<AppleConfig> getAppleConfig() {
            return null;
        }

        @Override
        public void start() {

        }

        @Override
        public void shutdown() {

        }
    }
}
