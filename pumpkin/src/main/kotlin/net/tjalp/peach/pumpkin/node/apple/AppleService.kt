package net.tjalp.peach.pumpkin.node.apple

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.tjalp.peach.peel.network.PeachRPC
import net.tjalp.peach.proto.apple.Apple
import net.tjalp.peach.proto.apple.Apple.AppleHandshakeRequest
import net.tjalp.peach.proto.apple.Apple.AppleHandshakeResponse
import net.tjalp.peach.proto.apple.AppleServiceGrpc.AppleServiceImplBase
import net.tjalp.peach.pumpkin.PumpkinServer
import java.net.InetSocketAddress
import java.util.*

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

    override fun playerSwitch(
        request: Apple.PlayerSwitchRequest,
        response: StreamObserver<Apple.PlayerSwitchResponse>
    ) {
        val uniqueId = UUID.fromString(request.playerUniqueIdentifier)
        val player = pumpkin.playerService.getPlayer(uniqueId)
        val targetNode = pumpkin.nodeService.getAppleNode(request.appleNodeIdentifier)

        if (player == null) {
            response.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Unknown player $uniqueId")
                    .asRuntimeException()
            )
            return
        }

        if (targetNode == null) {
            val res = Apple.PlayerSwitchResponse.newBuilder()
                .setSuccess(false)
                .build()

            response.onNext(res)
            response.onCompleted()
        } else {
            targetNode.connect(player)
            val res = Apple.PlayerSwitchResponse.newBuilder()
                .setSuccess(true)
                .build()

            response.onNext(res)
            response.onCompleted()
        }
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