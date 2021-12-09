package net.tjalp.peach.peel.database

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.lettuce.core.RedisClient
import io.lettuce.core.TransactionResult
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import net.tjalp.peach.peel.exception.InstanceDisposedException
import net.tjalp.peach.peel.signal.EmptySignal
import net.tjalp.peach.peel.util.GsonHelper
import org.slf4j.Logger
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import java.util.logging.Level
import kotlin.reflect.KClass

/**
 * Manages access to the Redis memory database.
 * The manager is able to access and mutate all
 * data located within the connected Redis process.
 *
 * Redis Querying is performed using [RedisReactiveCommands],
 * which is obtained by calling the [query] method.
 */
class RedisManager(
    logger: Logger,
    val nodeId: String,
    address: String = "127.0.0.1",
    port: Int = 6379,
    password: String = ""
) : Disposable {

    private var disposed = false
    private var client: RedisClient
    private var conn: StatefulRedisConnection<String, String>
    private var subscribe: StatefulRedisPubSubConnection<String, String>
    private var publish: StatefulRedisPubSubConnection<String, String>

    init {
        logger.info("Initializing Redis Manager")

        val startTime = System.currentTimeMillis()

        // Disable unformatted lettuce logging
        // breakig the console formatting.
        java.util.logging.Logger.getLogger("io.lettuce.core").level = Level.OFF

        this.client = RedisClient.create("redis://$password@$address:$port/0")
        this.conn = client.connect()
        this.subscribe = client.connectPubSub()
        this.publish = client.connectPubSub()

        logger.info("Initialized Redis Manager (took ${System.currentTimeMillis() - startTime}ms)")
    }

    /**
     * Returns the underlying [StatefulRedisConnection] instance
     */
    fun query(): RedisReactiveCommands<String, String> {
        if (isDisposed) {
            throw InstanceDisposedException()
        }

        return conn.reactive()
    }

    /**
     * Returns the underlying [StatefulRedisConnection] instance
     * for use in coroutines.
     */
    @Suppress("EXPERIMENTAL_API_USAGE")
    val query: RedisCoroutinesCommands<String, String>
        get() {
            if (isDisposed) {
                throw InstanceDisposedException()
            }

            return conn.coroutines()
        }

    /**
     * Perform a transactional query
     *
     * @param handle Transaction handler
     * @return Result
     */
    fun transactionLegacy(handle: RedisReactiveCommands<String, String>.() -> Unit): Mono<TransactionResult> {
        if (isDisposed) {
            throw InstanceDisposedException()
        }

        val query = query()

        query.multi().subscribe()
        handle(query)
        return query.exec()
    }

    /**
     * Perform a transactional query
     *
     * @param handle Transaction handler
     * @return Result
     */
    @Suppress("EXPERIMENTAL_API_USAGE")
    suspend fun transaction(handle: suspend RedisCoroutinesCommands<String, String>.() -> Unit): TransactionResult {
        if (isDisposed) {
            throw InstanceDisposedException()
        }

        query.multi()
        handle(query)
        return query.exec()
    }

    /**
     * Publish a [Signal] to the specified channel with no
     * additional payload
     *
     * @param key The PubSub key
     * @param meta Optional namespace meta
     */
    fun publish(key: SignalKey<EmptySignal>, meta: String? = null): Mono<Long> {
        return publish(key, EmptySignal(), meta)
    }

    /**
     * Publish a [Signal] to the specified channel
     *
     * @param key The PubSub key
     * @param signal The signal to transmit
     * @param meta Optional namespace meta
     */
    fun <T : Any> publish(key: SignalKey<T>, signal: T, meta: String? = null): Mono<Long> {
        val signalEl = GsonHelper.global().toJsonTree(signal)
        val signalObj = JsonObject()

        signalObj.add(SENDER_KEY, JsonPrimitive(nodeId))
        signalObj.add(PAYLOAD_KEY, signalEl)

        return publish.reactive().publish(buildNamespace(key, meta), GsonHelper.global().toJson(signalObj))
    }

    /**
     * Subscribes to the specified channel and returns a Flux
     * stream containing [SignalMessage] instances, allowing
     * the obtaining of both the [Signal] and the sender identity
     *
     * @param key The PubSub key
     * @param meta Optional namespace meta
     * @return Signal stream
     */
    fun <T : Any> subscribe(key: SignalKey<T>, meta: String? = null): Flux<SignalMessage<T>> {
        val namespace: String = buildNamespace(key, meta)

        subscribe.reactive().subscribe(namespace).subscribe()

        return subscribe.reactive().observeChannels()
            .filter { it.channel == namespace }
            .map {
                val signalObj = GsonHelper.global().fromJson(it.message, JsonObject::class.java)

                return@map SignalMessage(
                    signalObj[SENDER_KEY].asString,
                    GsonHelper.global().fromJson(signalObj[PAYLOAD_KEY], key.type.java)
                )
            }
    }

    /**
     * Unsubscribe from the given channel
     *
     * @param channel The channel
     */
    fun unsubscribe(channel: String) {
        subscribe.reactive().unsubscribe(channel).subscribe()
    }

    /**
     * Returns whether the manager has been disposed of
     *
     * @return Boolean
     */
    override fun isDisposed(): Boolean {
        return disposed
    }

    /**
     * Dispose and terminate the manager and its
     * internal resources
     */
    override fun dispose() {
        if (isDisposed) {
            throw InstanceDisposedException()
        }

        conn.close()
        client.shutdown()
        disposed = true
    }

    /**
     * Build a namespace string with the given params
     *
     * @param signal The signal key
     * @param meta The optional meta string
     * @return The namespace
     */
    private fun buildNamespace(signal: SignalKey<*>, meta: String? = null): String {
        return if (meta == null) signal.namespace else "${signal.namespace}:$meta"
    }

    /**
     * Represents a single PubSub channel that
     * can be subscribed and published to
     */
    class SignalKey<T : Any>(
        val namespace: String,
        val type: KClass<T>
    )

    /**
     * The contents of a received signal including
     * the payload and the sender node id
     */
    class SignalMessage<T>(
        val sender: String,
        val payload: T
    )

    companion object {
        const val SENDER_KEY = "s"
        const val PAYLOAD_KEY = "p"
    }

}