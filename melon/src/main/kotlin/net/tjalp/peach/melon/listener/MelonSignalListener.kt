package net.tjalp.peach.melon.listener

import net.tjalp.peach.melon.MelonServer
import net.tjalp.peach.peel.APPLE_NODE_REGISTER
import net.tjalp.peach.peel.APPLE_NODE_UNREGISTER
import net.tjalp.peach.peel.PLAYER_SWITCH
import net.tjalp.peach.peel.database.RedisManager.SignalMessage
import net.tjalp.peach.peel.signal.AppleNodeRegisterSignal
import net.tjalp.peach.peel.signal.AppleNodeUnregisterSignal
import net.tjalp.peach.peel.signal.PlayerSwitchSignal

class MelonSignalListener(
    private val melon: MelonServer
) {

    init {
        val redis = melon.redis

        redis.subscribe(APPLE_NODE_REGISTER).subscribe(this::appleNodeRegister)
        redis.subscribe(APPLE_NODE_UNREGISTER).subscribe(this::appleNodeUnregister)
        redis.subscribe(PLAYER_SWITCH).subscribe(this::onPlayerSwitch)
    }

    private fun appleNodeRegister(signal: SignalMessage<AppleNodeRegisterSignal>) {
        val payload = signal.payload

        melon.registerAppleNode(payload.nodeIdentifier, payload.server, payload.port)
    }

    private fun appleNodeUnregister(signal: SignalMessage<AppleNodeUnregisterSignal>) {
        val payload = signal.payload

        melon.unregisterAppleNode(payload.nodeIdentifier)
    }

    private fun onPlayerSwitch(signal: SignalMessage<PlayerSwitchSignal>) {
        val payload = signal.payload
        val player = melon.proxy.getPlayer(payload.uniqueId)
        val server = melon.proxy.getServer(payload.nodeId)

        if (player.isEmpty) return

        if (server.isEmpty) {
            melon.logger.error("Target node (${payload.nodeId}) does not exist!")
            return
        }

        player.get().createConnectionRequest(server.get()).connectWithIndication()
    }
}