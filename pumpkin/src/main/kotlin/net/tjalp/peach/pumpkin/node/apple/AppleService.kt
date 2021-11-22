package net.tjalp.peach.pumpkin.node.apple

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.tjalp.peach.proto.apple.Apple
import net.tjalp.peach.proto.apple.AppleServiceGrpc.AppleServiceImplBase
import net.tjalp.peach.pumpkin.PumpkinServer

class AppleService(
    private val pumpkin: PumpkinServer
) : AppleServiceImplBase() {

    override fun playerHandshake(request: Apple.PlayerHandshakeRequest, response: StreamObserver<Empty>) {
        pumpkin.logger.info("Received player handshake with (node type: apple, player name: ${request.playerName}, unique identifier: ${request.uuid})")
    }
}