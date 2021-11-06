package net.tjalp.peach.peel.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.lettuce.core.RedisClient;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.tjalp.peach.peel.config.RedisDetails;
import org.slf4j.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.function.Consumer;

// TODO Finish & Javadocs
public class RedisManager implements Disposable {

    private static final String SENDER_KEY = "s";
    private static final String PAYLOAD_KEY = "p";

    private boolean disposed = false;

    private final String nodeId;

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final StatefulRedisPubSubConnection<String, String> subscribe;
    private final StatefulRedisPubSubConnection<String, String> publish;

    public RedisManager(Logger logger, String nodeId) {
        this(logger, nodeId, "localhost", 6379, "");
    }

    public RedisManager(Logger logger, String nodeId, RedisDetails details) {
        this(logger, nodeId, details.server, details.port, details.password);
    }

    public RedisManager(Logger logger, String nodeId, String address, int port, String password) {
        logger.info("Initializing Redis Manager...");

        long startTime = System.currentTimeMillis();

        this.nodeId = nodeId;

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

    /**
     * Returns the underlying [StatefulRedisConnection] instance
     */
    public RedisReactiveCommands<String, String> query() {
        if(isDisposed()) throw new IllegalStateException("Cannot query when disposed");

        return connection.reactive();
    }

    public Mono<TransactionResult> transaction(Consumer<RedisReactiveCommands<String, String>> handle) {
        if(isDisposed()) throw new IllegalStateException("Cannot query when disposed");

        RedisReactiveCommands<String, String> query = query();

        query.multi().subscribe();
        handle.accept(query);
        return query.exec();
    }

    public <T> Mono<Long> publish(SignalKey<T> key, T signal) {
        return publish(key, signal, null);
    }

    public <T> Mono<Long> publish(SignalKey<T> key, T signal, @Nullable String meta) {
        JsonElement signalEl = new Gson().toJsonTree(signal);
        JsonObject signalObj = new JsonObject();

        signalObj.add(SENDER_KEY, new JsonPrimitive(nodeId));
        signalObj.add(PAYLOAD_KEY, signalEl);

        return publish.reactive().publish(buildNamespace(key, meta), new Gson().toJson(signalObj));
    }

    public <T> Flux<SignalMessage<T>> subscribe(SignalKey<T> key) {
        return subscribe(key, null);
    }

    public <T> Flux<SignalMessage<T>> subscribe(SignalKey<T> key, @Nullable String meta) {
        String namespace = buildNamespace(key, meta);

        subscribe.reactive().subscribe(namespace).subscribe();

        return subscribe.reactive().observeChannels()
                .filter(message -> message.getChannel().equals(namespace))
                .map(message -> {
                    Gson gson = new Gson();
                    JsonObject signalObj = gson.fromJson(message.getMessage(), JsonObject.class);

                    return new SignalMessage<>(
                            signalObj.get(SENDER_KEY).getAsString(),
                            gson.fromJson(signalObj.get(PAYLOAD_KEY), key.type)
                    );
                });
    }

    public void unsubscribe(String channel) {
        subscribe.reactive().unsubscribe(channel).subscribe();
    }

    private <T> String buildNamespace(SignalKey<T> signal, @Nullable String meta) {
        return meta == null ? signal.namespace : signal.namespace + ":" + meta;
    }

    record SignalKey<T>(String namespace, Class<T> type) {}

    record SignalMessage<T>(String sender, T payload) {}
}
