package net.tjalp.peach.peel.network

import io.grpc.Context
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.StreamObserver
import net.tjalp.peach.peel.config.PumpkinDetails
import org.slf4j.Logger

object PeachRPC {

    val NODE_ID_KEY = metadataKey("Node")
    val NODE_ID_CTX = contextKey<String>("Node Identifier")

    // Key generators
    private fun metadataKey(name: String) : Metadata.Key<String> = Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER)
    private fun <T> contextKey(name: String) : Context.Key<T> = Context.key(name)

    /**
     * Create a new [StreamObserver] with the
     * supplied callbacks
     */
    fun <T> streamObserver(
        onNext: (T) -> Unit = {},
        onError: (Throwable) -> Unit = {},
        onComplete: () -> Unit = {}) : StreamObserver<T> {
        return object : StreamObserver<T> {

            override fun onNext(value: T) = onNext(value)

            override fun onError(t: Throwable) = onError(t)

            override fun onCompleted() = onComplete()

        }
    }

    /**
     * Create a new RPC Channel that connects
     * to the specified Pumpkin details
     *
     * @param nodeId The node id
     * @param logger The module logger
     * @param config The PumpkinDetails
     * @return The ManagedChannelBuilder
     */
    fun createChannel(nodeId: String, logger: Logger, config: PumpkinDetails): ManagedChannelBuilder<*> {
        return ManagedChannelBuilder.forAddress(config.server, config.port)
            .intercept(ClientNodeInterceptor(nodeId, logger))
            .usePlaintext()
    }
}