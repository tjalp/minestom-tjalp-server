package net.tjalp.peach.peel.database;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.slf4j.Logger;

public class RedisManager {

    private Logger logger;

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String, String> subscribe;
    private StatefulRedisPubSubConnection<String, String> publish;

    public RedisManager(Logger logger) {
        this.logger = logger;

        logger.info("Initializing Redis Manager...");

        long startTime = System.currentTimeMillis();

        String address = System.getProperty("redisAddress", "127.0.0.1");
        String port = System.getProperty("redisPort", "6379");
        String password = System.getProperty("redisPassword", "");

        this.client = RedisClient.create("redis://" + password + "@" + address + ":" + port + "/0");
        this.connection = this.client.connect();
        this.subscribe = this.client.connectPubSub();
        this.publish = this.client.connectPubSub();

        long timeTook = System.currentTimeMillis() - startTime;

        logger.info("Initialized Redis Manager (took " + timeTook + "ms)");
    }
}
