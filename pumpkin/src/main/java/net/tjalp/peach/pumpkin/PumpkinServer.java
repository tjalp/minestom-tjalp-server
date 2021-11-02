package net.tjalp.peach.pumpkin;

import net.tjalp.peach.peel.config.JsonConfig;
import net.tjalp.peach.peel.database.RedisManager;
import net.tjalp.peach.peel.util.Check;
import net.tjalp.peach.pumpkin.config.PumpkinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Scanner;

/**
 * The main class used for all kinds of things that need to be synchronized
 */
public class PumpkinServer {

    private static PumpkinServer instance;

    public static PumpkinServer get() {
        return PumpkinServer.instance;
    }

    private Logger logger;
    private boolean initialized = false;
    private boolean isRunning = false;

    /** The pumpkin config */
    private JsonConfig<PumpkinConfig> config;

    /** The redis manager service */
    private RedisManager redis;

    /**
     * Initialize the PumpkinServer
     */
    public void init(File configFile) {
        this.logger = LoggerFactory.getLogger(PumpkinServer.class);
        this.config = new JsonConfig<>(configFile, PumpkinConfig.class);

        // Redis
        System.setProperty("redisAddress", config().redis.server);
        System.setProperty("redisPort", String.valueOf(config().redis.port));
        System.setProperty("redisPassword", config().redis.password);

        // Initialize various services
        this.redis = new RedisManager(logger);

        // Set the initialized state to true
        this.initialized = true;

        logger.info("Initialized pumpkin");
    }

    /**
     * Start the PumpkinServer
     */
    public void start(String address, int port) {
        Check.stateCondition(!initialized, "PumpkinServer#init must be used before using PumpkinServer#start");

        isRunning = true;

        logger.info("Started PumpkinServer");

        while (isRunning) {
            Scanner scanner = new Scanner(System.in);

            scanner.nextLine();
        }
    }

    /**
     * Shut down the PumpkinServer
     */
    public void shutdown() {

        logger.info("Shutting down services");

        redis().dispose();
    }

    public Logger logger() {
        return this.logger;
    }

    public RedisManager redis() {
        return this.redis;
    }

    public PumpkinConfig config() {
        return this.config.data();
    }
}
