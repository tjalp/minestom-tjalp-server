package net.tjalp.peach.pumpkin;

import net.tjalp.peach.peel.database.RedisManager;
import net.tjalp.peach.peel.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * The main class used for all kinds of things that need to be synchronized
 */
public class PumpkinServer {

    private Logger logger;
    private boolean initialized = false;
    private boolean isRunning = false;

    /** The redis manager service */
    private RedisManager redis;

    /**
     * Initialize the PumpkinServer
     */
    public void init() {
        this.logger = LoggerFactory.getLogger(PumpkinServer.class);

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

    public Logger logger() {
        return this.logger;
    }

    public RedisManager redis() {
        return this.redis;
    }
}
