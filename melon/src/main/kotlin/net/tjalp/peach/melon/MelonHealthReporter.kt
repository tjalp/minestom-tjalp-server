package net.tjalp.peach.melon

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.tjalp.peach.peel.network.HealthReporter
import net.tjalp.peach.proto.melon.Melon.MelonHealthReport
import net.tjalp.peach.proto.melon.MelonServiceGrpc
import net.tjalp.peach.proto.melon.MelonServiceGrpc.MelonServiceStub

class MelonHealthReporter(
    private val melon: MelonServer
) : HealthReporter<MelonHealthReport>(melon.logger) {

    private val rpcStub: MelonServiceStub = MelonServiceGrpc.newStub(melon.rpcChannel)

    override fun openHealthStream() {
        GlobalScope.launch {
            melon.sendMelonHandshake()
            super.openHealthStream()
        }
    }

    override fun initCall(listener: StreamObserver<Empty>): StreamObserver<MelonHealthReport> {
        return rpcStub.healthStatus(listener)
    }

    override fun buildReport(): MelonHealthReport {
        return MelonHealthReport.newBuilder().build()
    }

    override fun connectionFailed() {
        melon.rpcChannel.enterIdle()
    }
}