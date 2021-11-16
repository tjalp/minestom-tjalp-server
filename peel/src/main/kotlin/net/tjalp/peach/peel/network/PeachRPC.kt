package net.tjalp.peach.peel.network

import io.grpc.ManagedChannelBuilder
import net.tjalp.peach.peel.config.PumpkinDetails
import org.slf4j.Logger

object PeachRPC {

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