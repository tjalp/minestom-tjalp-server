package net.tjalp.peach.melon.listener

import net.tjalp.peach.melon.MelonServer
import net.tjalp.peach.peel.APPLE_NODE_REGISTER
import net.tjalp.peach.peel.APPLE_NODE_UNREGISTER
import net.tjalp.peach.peel.database.RedisManager.SignalMessage
import net.tjalp.peach.peel.signal.AppleNodeRegisterSignal
import net.tjalp.peach.peel.signal.AppleNodeUnregisterSignal

class MelonSignalListener(
    private val melon: MelonServer
) {

    init {
        val redis = melon.redis

        redis.subscribe(APPLE_NODE_REGISTER).subscribe(this::appleNodeRegister)
        redis.subscribe(APPLE_NODE_UNREGISTER).subscribe(this::appleNodeUnregister)
    }

    private fun appleNodeRegister(signal: SignalMessage<AppleNodeRegisterSignal>) {
        val payload = signal.payload

        melon.registerAppleNode(payload.nodeIdentifier, payload.server, payload.port)
    }

    private fun appleNodeUnregister(signal: SignalMessage<AppleNodeUnregisterSignal>) {
        val payload = signal.payload

        melon.unregisterAppleNode(payload.nodeIdentifier)
    }
}