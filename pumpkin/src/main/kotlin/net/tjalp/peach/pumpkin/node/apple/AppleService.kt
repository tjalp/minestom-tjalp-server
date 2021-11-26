package net.tjalp.peach.pumpkin.node.apple

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.tjalp.peach.peel.network.PeachRPC
import net.tjalp.peach.proto.apple.Apple
import net.tjalp.peach.proto.apple.Apple.AppleHandshakeRequest
import net.tjalp.peach.proto.apple.Apple.AppleHandshakeResponse
import net.tjalp.peach.proto.apple.AppleServiceGrpc.AppleServiceImplBase
import net.tjalp.peach.pumpkin.PumpkinServer
import java.net.InetSocketAddress

class AppleService(
    private val pumpkin: PumpkinServer
) : AppleServiceImplBase() {

    override fun healthStatus(response: StreamObserver<Empty>): StreamObserver<Apple.AppleHealthReport> {
        return current().healthMonitor.listen(response)
    }

    override fun appleHandshake(request: AppleHandshakeRequest, response: StreamObserver<AppleHandshakeResponse>) {
        val socket = currentInetSocketAddress()
        val appleNode = AppleServerNode(
            pumpkin,
            request.nodeIdentifier,
            socket.address.hostAddress,
            request.port,
        )

        pumpkin.nodeService.register(appleNode)

        val res = AppleHandshakeResponse.newBuilder()
            .build()

        response.onNext(res)
        response.onCompleted()
    }

    /**
     * Resolves the apple instance that sent
     * a call by parsing the Node ID from the
     * current context
     *
     * @return The apple node which sent the request
     */
    private fun current(): AppleNode {
        return pumpkin.nodeService.getAppleNode(PeachRPC.NODE_ID_CTX.get())!!
    }

    /**
     * Resolves the address the request was
     * made from
     *
     * @return The [InetSocketAddress] of this request
     */
    private fun currentInetSocketAddress(): InetSocketAddress {
        return PeachRPC.INET_SOCKET_CTX.get()
    }
}