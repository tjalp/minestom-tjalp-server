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
    private val tempAppleNode: AppleNode = AppleServerNode()

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