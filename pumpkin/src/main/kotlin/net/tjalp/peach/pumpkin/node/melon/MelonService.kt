package net.tjalp.peach.pumpkin.node.melon

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.tjalp.peach.peel.network.PeachRPC
import net.tjalp.peach.proto.melon.Melon.*
import net.tjalp.peach.proto.melon.MelonServiceGrpc.MelonServiceImplBase
import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.player.ConnectedPlayer
import java.net.InetSocketAddress
import java.util.*

class MelonService(
    private val pumpkin: PumpkinServer
) : MelonServiceImplBase() {

    override fun healthStatus(response: StreamObserver<Empty>): StreamObserver<MelonHealthReport> {
        return current().healthMonitor.listen(response)
    }

    override fun melonHandshake(
        request: MelonHandshakeRequest,
        response: StreamObserver<MelonHandshakeResponse>
    ) {
        val socket = currentInetSocketAddress()
        val hostAddress = socket.address.hostAddress
        val melonNode = MelonServerNode(
            pumpkin,
            pumpkin.dockerService.registeredNodes.first {
                it.details.server == hostAddress
            },
            request.nodeIdentifier,
            request.port
        )

        // Register the melon node
        pumpkin.nodeService.register(melonNode)

        for (player in request.playerList) {
            val nodeId = player.currentAppleNode
            val appleNode = pumpkin.nodeService.getAppleNode(nodeId)

            if (appleNode == null) {
                pumpkin.logger.error("Tried to register player on an unregistered apple node (node identifier: $nodeId)!")
                return
            }

            pumpkin.playerService.register(
                uniqueId = UUID.fromString(player.uniqueId),
                username = player.username,
                melonNode = melonNode,
                appleNode = appleNode
            )
        }

        val appleNodeRegistrations = pumpkin.nodeService.appleNodes
            .map {
                AppleNodeRegistration.newBuilder()
                    .setNodeId(it.nodeIdentifier)
                    .setServer(it.server)
                    .setPort(it.port)
                    .build()
            }

        val res = MelonHandshakeResponse.newBuilder()
            .addAllAppleNodeRegistration(appleNodeRegistrations)
            .build()

        response.onNext(res)
        response.onCompleted()
    }

    override fun playerHandshake(request: PlayerHandshakeRequest, response: StreamObserver<PlayerHandshakeResponse>) {
        val res = PlayerHandshakeResponse.newBuilder()
        val targetAppleNode = pumpkin.nodeService.appleNodes.firstOrNull()

        if (targetAppleNode == null) {
            response.onNext(res.setTargetNodeIdentifier("").build())
            response.onCompleted()
            return
        } else {
            res.targetNodeIdentifier = targetAppleNode.nodeIdentifier
        }

        pumpkin.playerService.register(
            UUID.fromString(request.uniqueId),
            request.username,
            current(),
            targetAppleNode
        )

        response.onNext(res.build())
        response.onCompleted()
    }

    override fun playerDisconnect(request: PlayerQuit, response: StreamObserver<Empty>) {
        val player = pumpkin.playerService.getPlayer(UUID.fromString(request.uniqueId))

        if (player != null) {
            pumpkin.playerService.unregister(player as ConnectedPlayer)
        }

        response.onNext(Empty.getDefaultInstance())
        response.onCompleted()
    }

    /**
     * Resolves the melon instance that sent
     * a call by parsing the Node ID from the
     * current context
     *
     * @return The melon node which sent the request
     */
    private fun current() : MelonNode {
        return pumpkin.nodeService.getMelonNode(PeachRPC.NODE_ID_CTX.get())!!
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