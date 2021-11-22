package net.tjalp.peach.peel.network

import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import net.tjalp.peach.peel.config.PumpkinDetails
import org.slf4j.Logger

object PeachRPC {

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
     * @param logger The module logger
     * @param config The PumpkinDetails
     * @return The ManagedChannelBuilder
     */
    fun createChannel(logger: Logger, config: PumpkinDetails): ManagedChannelBuilder<*> {
        return ManagedChannelBuilder.forAddress(config.server, config.port)
            .usePlaintext()
    }
}