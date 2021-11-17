package net.tjalp.peach.pumpkin.node.melon

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.tjalp.peach.proto.melon.Melon
import net.tjalp.peach.proto.melon.MelonServiceGrpc.MelonServiceImplBase
import net.tjalp.peach.pumpkin.PumpkinServer

class MelonService(
    private val pumpkin: PumpkinServer
) : MelonServiceImplBase() {

    override fun playerHandshake(request: Melon.PlayerHandshakeRequest, responseObserver: StreamObserver<Empty>) {
        pumpkin.logger.info("Received player handshake with (node type: melon, player name: ${request.playerName}, unique identifier: ${request.uuid})")
    }
}