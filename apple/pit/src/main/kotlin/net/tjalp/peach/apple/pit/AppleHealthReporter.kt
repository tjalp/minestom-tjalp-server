package net.tjalp.peach.apple.pit

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.tjalp.peach.peel.network.HealthReporter
import net.tjalp.peach.proto.apple.Apple
import net.tjalp.peach.proto.apple.AppleServiceGrpc

class AppleHealthReporter(
    private val apple: AppleServer
) : HealthReporter<Apple.AppleHealthReport>(apple.logger) {

    private val rpcStub: AppleServiceGrpc.AppleServiceStub = AppleServiceGrpc.newStub(apple.rpcChannel)

    override fun openHealthStream() {
        GlobalScope.async {
            apple.sendAppleHandshake()
            super.openHealthStream()
        }
    }

    override fun initCall(listener: StreamObserver<Empty>): StreamObserver<Apple.AppleHealthReport> {
        return rpcStub.healthStatus(listener)
    }

    override fun buildReport(): Apple.AppleHealthReport {
        return Apple.AppleHealthReport.newBuilder().build()
    }

    override fun connectionFailed() {
        apple.rpcChannel.enterIdle()
    }
}