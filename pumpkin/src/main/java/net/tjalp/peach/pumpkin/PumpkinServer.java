package net.tjalp.peach.pumpkin;

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

    /**
     * Initialize the PumpkinServer
     */
    public void init() {
        this.logger = LoggerFactory.getLogger(PumpkinServer.class);

        this.initialized = true;

        logger.info("Initialized pumpkin");
    }

    /**
     * Start the PumpkinServer
     */
    public void start(String address, int port) {
        Check.stateCondition(!initialized, "PumpkinServer#init must be used before using PumpkinServer#start");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        logger.info("Started PumpkinServer");
    }

    public Logger logger() {
        return this.logger;
    }
}
