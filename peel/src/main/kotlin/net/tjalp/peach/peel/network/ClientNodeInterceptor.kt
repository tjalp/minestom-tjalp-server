package net.tjalp.peach.peel.network

import io.grpc.*
import org.slf4j.Logger

class ClientNodeInterceptor(
    val nodeId: String,
    val logger: Logger
) : ClientInterceptor {

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        val call = next.newCall(method, callOptions)

        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {

            override fun start(listener: Listener<RespT>, headers: Metadata) {
                headers.put(PeachRPC.NODE_ID_KEY, nodeId)

                logger.info("Sending ${method.fullMethodName} call to server")

                super.start(listener, headers)
            }
        }
    }
}