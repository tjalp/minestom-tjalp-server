package net.tjalp.peach.apple.pit.listener

import net.tjalp.peach.apple.pit.AppleServer
import net.tjalp.peach.peel.REQUEST_PUMPKIN_CONNECT
import net.tjalp.peach.peel.database.RedisManager
import net.tjalp.peach.peel.signal.EmptySignal

class AppleSignalListener(
    private val apple: AppleServer
) {

    init {
        val redis = apple.redis

        redis.subscribe(REQUEST_PUMPKIN_CONNECT).subscribe(this::requestPumpkinConnect)
    }

    private fun requestPumpkinConnect(signal: RedisManager.SignalMessage<EmptySignal>) {
        apple.healthReporter.connect()
    }
}