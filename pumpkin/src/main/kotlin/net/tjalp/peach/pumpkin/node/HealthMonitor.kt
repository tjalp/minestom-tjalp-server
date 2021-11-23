package net.tjalp.peach.pumpkin.node

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import net.tjalp.peach.peel.network.PeachRPC.streamObserver
import net.tjalp.peach.pumpkin.PumpkinServer
import org.slf4j.Logger
import reactor.core.publisher.DirectProcessor

/**
 * The HealthMonitor tracks the health of a
 * remote node
 */
class HealthMonitor<T>(val node: Node) {

    private val logger: Logger = PumpkinServer.get().logger

    /**
     * The online status of the node
     */
    @Volatile var isOnline: Boolean = false
        private set

    /**
     * Listen for connection drop signals
     */
    var onConnectionDrop: DirectProcessor<Unit> = DirectProcessor.create()
        private set

    /**
     * Listen for connection open signals
     */
    var onConnectionOpen: DirectProcessor<T> = DirectProcessor.create()
        private set

    /**
     * Listen for incoming health reports
     */
    var onReport: DirectProcessor<T> = DirectProcessor.create()
        private set

    /**
     * The observer handle
     */
    private var handle: ServerCallStreamObserver<Empty>? = null

    /**
     * Holds whether the client is notified of an initial success
     */
    private var isNotifed: Boolean = false

    /**
     * Listen for health reports
     */
    fun listen(response: StreamObserver<Empty>) : StreamObserver<T> {
        handle = response as ServerCallStreamObserver

        // When a cancellation event is received and
        // the response is still current, this means
        // we have lost connection to the node.
        response.setOnCancelHandler {
            if(handle == response) {
                dropConnection()
            }
        }

        return streamObserver(
            onNext = {
                // Mark the connection as open
                openConnection(it)

                // Pass the report to subscribers
                onReport.onNext(it)

                // Notify the client, allowing them to
                // mark the connection as successful
                if(!isNotifed) {
                    isNotifed = true
                    response.onNext(Empty.getDefaultInstance())
                }
            }
        )
    }

    /**
     * Mark the connection as opened
     *
     * @param report The initial report
     */
    private fun openConnection(report: T) {
        if(isOnline) return
        isOnline = true

        logger.info("Connection to ${node.nodeId} opened")

        onConnectionOpen.onNext(report)
    }

    /**
     * Mark the connection as dropped and
     * react accordingly
     */
    private fun dropConnection() {
        if(!isOnline) return
        isOnline = false
        isNotifed = false

        logger.info("Lost connection to ${node.nodeId}")

        onConnectionDrop.onNext(Unit)
    }

    /**
     * Terminate the monitor connection
     */
    fun close() {
        handle?.onError(Status.CANCELLED.asException())
    }

}