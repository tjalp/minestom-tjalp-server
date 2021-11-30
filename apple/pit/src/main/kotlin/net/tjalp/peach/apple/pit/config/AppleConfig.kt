package net.tjalp.peach.apple.pit.config

import net.tjalp.peach.peel.config.NodeConfig
import net.tjalp.peach.peel.config.PumpkinDetails
import net.tjalp.peach.peel.config.RedisDetails

abstract class AppleConfig : NodeConfig() {

    /**
     * The pumpkin details
     */
    var pumpkin: PumpkinDetails = PumpkinDetails()

    /**
     * Redis connection instructions
     */
    var redis: RedisDetails = RedisDetails()
}