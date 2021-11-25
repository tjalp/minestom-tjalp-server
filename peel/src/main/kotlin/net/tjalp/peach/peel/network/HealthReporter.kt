package net.tjalp.peach.peel.network

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.ClientCallStreamObserver
import io.grpc.stub.StreamObserver
import net.tjalp.peach.peel.network.PeachRPC.streamObserver
import org.slf4j.Logger
import reactor.core.publisher.DirectProcessor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract class HealthReporter<T>(
    val logger: Logger
) {

    /**
     * Holds whether the reporter is currently active
     */
    var isRunning: Boolean = false
        private set

    /**
     * Holds whether the node is connected to pumpkin
     */
    var isConnected: Boolean = false
        private set

    /**
     * Listen for connection drop signals
     */
    var onConnectionDrop: DirectProcessor<Unit> = DirectProcessor.create()
        private set

    /**
     * Listen for connection open signals
     */
    var onConnectionOpen: DirectProcessor<Unit> = DirectProcessor.create()
        private set

    /**
     * The timer instance
     */
    private val heartbeat = Executors.newSingleThreadScheduledExecutor()

    /**
     * The currently open health stream
     */
    private var healthStream: StreamObserver<T>? = null

    /**
     * The timestamp at which the connection was opened
     */
    private var connectionStartTime: Long = 0

    /**
     * Start the HealthReporter task which will
     * post health statistics to pumpkin at a
     * fixed interval of 5 seconds.
     */
    fun start() {
        if (isRunning) return
        isRunning = true

        logger.info("Starting RPC Queue processing")

        // Start the health task which will
        // either attempt a new connection,
        // or send a health report every 5s
        heartbeat.scheduleAtFixedRate({
            if (isConnected) {
                sendHealthReport()
            }
        }, 1, 5, TimeUnit.SECONDS)
    }

    /**
     * Stop processing the health reporting service
     */
    fun stop() {
        if (!isRunning) return

        isRunning = false
        heartbeat.shutdown()
    }

    /**
     * Attempt to connect to the remote pumpkin node
     */
    fun connect() {
        if (isConnected) return
        val stream = healthStream

        if (stream is ClientCallStreamObserver) {
            stream.onCompleted()
            stream.cancel("Health report reset", null)
        }

        openHealthStream()
    }

    /**
     * Send a health report to the server and
     * optionally launch a new connection if
     * the stream becomes stale.
     */
    private fun sendHealthReport() {
        healthStream!!.onNext(buildReport())
    }

    /**
     * Attempt to initialize a new health
     * connection to pumpkin in order to
     * send health update reports
     */
    open fun openHealthStream() {
        logger.info("Attempting new connection")

        // Instantiate a new call and await the
        // confirmation response message.
        healthStream = initCall(streamObserver(
            onNext = {
                connectionStartTime = System.currentTimeMillis()
                logger.info("Connection established successfully")

                val wasConnected = isConnected

                isConnected = true

                if (!wasConnected) {
                    onConnectionOpen.onNext(Unit)
                }
            },
            onError = { err ->
                if (err is StatusException && err.status == Status.CANCELLED) {
                    logger.warn("Connection was cancelled")
                } else {
                    logger.error("Connection dropped (${err.message})")
                }

                val wasConnected = isConnected

                isConnected = false

                if (wasConnected) {
                    onConnectionDrop.onNext(Unit)
                } else {
                    connectionFailed()
                }
            }
        ))

        // Send our initial report
        healthStream!!.onNext(buildReport())
    }

    /**
     * Post a health report to the remote
     * pumpkin instance and returns the
     * stream observer.
     *
     * The reporter will monitor each request
     * for a failure, indicating that the
     * pumpkin instance is offline.
     *
     * @param listener The response observer
     * @return Any StreamObserver
     */
    abstract fun initCall(listener: StreamObserver<Empty>): StreamObserver<T>

    /**
     * Called whenever a new health report
     * is required to be sent.
     *
     * @return The health message
     */
    abstract fun buildReport(): T

    /**
     * Called when a request to connect to
     * pumpkin failed.
     */
    abstract fun connectionFailed()

}