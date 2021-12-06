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
import net.tjalp.peach.pumpkin.node.Node
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
        val hostAddress = socket.address.hostAddress
        val appleNode = AppleServerNode(
            pumpkin,
            pumpkin.dockerService.registeredNodes.first {
                it.details.server == hostAddress
            },
            request.nodeIdentifier,
            // We're running in a container, so 127.0.0.1 is redundant
            if (hostAddress.equals("127.0.0.1")) "127.0.0.1" else hostAddress,
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

    override fun createNode(
        request: Apple.CreateNodeRequest,
        response: StreamObserver<Apple.CreateNodeResponse>
    ) {
        val type = Node.Type.values().firstOrNull {
            it.name == request.nodeType.uppercase()
        } ?: return
        val dockerNode = pumpkin.dockerService.randomDockerNode()
        val nodeId = request.nodeIdentifier

        val node = try {
            if (nodeId == "") {
                dockerNode.createNode(type)
            } else {
                dockerNode.createNode(
                    type = type,
                    nodeId = nodeId
                )
            }
        } catch (ex: Exception) {
            pumpkin.logger.error("An error occured while trying to create node: ${ex.message}")
            response.onNext(Apple.CreateNodeResponse.newBuilder()
                .setNodeType(type.fullName)
                .setNodeIdentifier(nodeId)
                .setSuccess(false).build())
            response.onCompleted()
            return
        }

        response.onNext(Apple.CreateNodeResponse.newBuilder()
            .setSuccess(true)
            .setNodeIdentifier(node.nodeId)
            .setNodeType(node.nodeType.fullName)
            .build()
        )
        response.onCompleted()
    }

    override fun stopNode(request: Apple.StopNodeRequest, response: StreamObserver<Empty>) {
        val node = pumpkin.nodeService.nodes.firstOrNull {
            it.nodeIdentifier == request.nodeIdentifier
        }

        node?.dockerNode?.stopNode(node)

        response.onNext(Empty.getDefaultInstance())
        response.onCompleted()
    }

    override fun killNode(request: Apple.KillNodeRequest, response: StreamObserver<Empty>) {
        val node = pumpkin.nodeService.nodes.firstOrNull {
            it.nodeIdentifier == request.nodeIdentifier
        }

        node?.dockerNode?.killNode(node)

        response.onNext(Empty.getDefaultInstance())
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