package net.tjalp.peach.peel.database;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.tjalp.peach.peel.config.RedisDetails;
import org.slf4j.Logger;
import reactor.core.Disposable;

// TODO Javadocs
public class RedisManager implements Disposable {

    private Logger logger;
    private boolean disposed = false;

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String, String> subscribe;
    private StatefulRedisPubSubConnection<String, String> publish;

    public RedisManager(Logger logger) {
        this(logger, "localhost", 6379, "");
    }

    public RedisManager(Logger logger, RedisDetails details) {
        this(logger, details.server, details.port, details.password);
    }

    public RedisManager(Logger logger, String address, int port, String password) {
        this.logger = logger;

        logger.info("Initializing Redis Manager...");

        long startTime = System.currentTimeMillis();

        // Connect to redis
        this.client = RedisClient.create("redis://" + password + "@" + address + ":" + port + "/0");
        this.connection = this.client.connect();
        this.subscribe = this.client.connectPubSub();
        this.publish = this.client.connectPubSub();

        long timeTook = System.currentTimeMillis() - startTime;

        logger.info("Initialized Redis Manager (took " + timeTook + "ms)");
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

    @Override
    public void dispose() {
        if (isDisposed()) throw new IllegalStateException("This Redis Manager is already disposed");

        connection.close();
        client.shutdown();
        disposed = true;
    }
}
