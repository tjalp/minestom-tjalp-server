package net.tjalp.peach.peel.network

import io.grpc.*
import org.slf4j.Logger
import java.net.InetSocketAddress

/**
 * Implements a [ServerInterceptor] which requires
 * the presence of a valid Node Identifier string.
 *
 * The ServerNodeInterceptor is expected to be used
 * by pumpkin in order to validate the client nodes.
 */
class ServerNodeInterceptor(
    val logger: Logger
) : ServerInterceptor {

    /**
     * Whether RPC calls should be logged
     */
    var logInboundCalls = false

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val nodeId = headers.get(PeachRPC.NODE_ID_KEY)
        val callName = call.methodDescriptor.fullMethodName.split("/").lastOrNull()

        // Resolve the source address
        val rawAddress = call.attributes[Grpc.TRANSPORT_ATTR_REMOTE_ADDR]!!
        val sockAddress = rawAddress as InetSocketAddress
        val address = sockAddress.address

        // Validate the node id
        if(nodeId == null) {
            logger.warn("Rejecting invalid call from $address")
            call.close(Status.UNAUTHENTICATED, headers)
            return BlankListener()
        }

        if(logInboundCalls) {
            logger.info("Received $callName call from $nodeId")
        }

        // Append the Node ID to the context
        val context = Context.current()
            .withValue(PeachRPC.NODE_ID_CTX, nodeId)

        return Contexts.interceptCall(context, call, headers, next)
    }

}