package net.tjalp.peach.pumpkin.node.melon

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.tjalp.peach.proto.melon.Melon
import net.tjalp.peach.proto.melon.MelonServiceGrpc.MelonServiceImplBase
import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.apple.AppleNode
import net.tjalp.peach.pumpkin.node.apple.AppleServerNode
import net.tjalp.peach.pumpkin.player.ConnectedPlayer
import java.util.*

class MelonService(
    private val pumpkin: PumpkinServer
) : MelonServiceImplBase() {

    private val tempMelonNode: MelonNode = MelonServerNode(pumpkin, "melon-1")
    private val tempAppleNode: AppleNode = AppleServerNode(pumpkin, "apple-1", "localhost", 25000).also {
        pumpkin.nodeService.register(it)
    }

    override fun healthStatus(response: StreamObserver<Empty>): StreamObserver<Melon.MelonHealthReport> {
        return pumpkin.nodeService.melonNodes.first().healthMonitor.listen(response)
    }

    override fun proxyHandshake(
        request: Melon.ProxyHandshakeRequest,
        response: StreamObserver<Melon.ProxyHandshakeResponse>
    ) {
        val melonNode = MelonServerNode(pumpkin, UUID.randomUUID().toString())

        // Register the melon node
        pumpkin.nodeService.register(melonNode)

        for (player in request.playerList) {
            pumpkin.playerService.register(
                uniqueId = UUID.fromString(player.uniqueId),
                username = player.username,
                melonNode = melonNode,
                appleNode = tempAppleNode // TODO Apple nodes
            )
        }

        val appleNodeRegistrations = pumpkin.nodeService.appleNodes
            //.filter { it.isOnline }
            .map {
                Melon.AppleNodeRegistration.newBuilder()
                    .setNodeId(it.nodeId)
                    .setServer(it.server)
                    .setPort(it.port)
                    .build()
            }

        val res = Melon.ProxyHandshakeResponse.newBuilder()
            .addAllAppleNodeRegistration(appleNodeRegistrations)
            .build()

        response.onNext(res)
        response.onCompleted()
    }

    override fun playerHandshake(request: Melon.PlayerHandshakeRequest, response: StreamObserver<Empty>) {
        pumpkin.playerService.register(
            UUID.fromString(request.uniqueId),
            request.username,
            tempMelonNode,
            tempAppleNode
        )

        response.onNext(Empty.getDefaultInstance())
        response.onCompleted()
    }

    override fun playerDisconnect(request: Melon.PlayerQuit, response: StreamObserver<Empty>) {
        val player = pumpkin.playerService.getPlayer(UUID.fromString(request.uniqueId))

        if (player != null) {
            pumpkin.playerService.unregister(player as ConnectedPlayer)
        }

        response.onNext(Empty.getDefaultInstance())
        response.onCompleted()
    }
}