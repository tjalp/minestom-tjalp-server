package net.tjalp.peach.pumpkin.node

import io.grpc.*
import net.tjalp.peach.pumpkin.PumpkinServer

class PumpkinSyncInterceptor(
    val pumpkin: PumpkinServer
) : ServerInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata?,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        return pumpkin.mainThread.awaitTask {
            object :
                ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {

                override fun onMessage(message: ReqT) {
                    pumpkin.mainThread.awaitTask {
                        super.onMessage(message)
                    }
                }

                override fun onHalfClose() {
                    pumpkin.mainThread.awaitTask {
                        super.onHalfClose()
                    }
                }

                override fun onComplete() {
                    pumpkin.mainThread.awaitTask {
                        super.onComplete()
                    }
                }

                override fun onCancel() {
                    pumpkin.mainThread.awaitTask {
                        super.onCancel()
                    }
                }

                override fun onReady() {
                    pumpkin.mainThread.awaitTask {
                        super.onReady()
                    }
                }

            }
        }
    }
}