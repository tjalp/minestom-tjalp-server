package net.tjalp.peach.peel.database;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
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
        this.logger = logger;

        logger.info("Initializing Redis Manager...");

        long startTime = System.currentTimeMillis();

        // Set the jvm properties for Redis
        String address = System.getProperty("redisAddress", "127.0.0.1");
        String port = System.getProperty("redisPort", "6379");
        String password = System.getProperty("redisPassword", "");

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
