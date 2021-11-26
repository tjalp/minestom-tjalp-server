package net.tjalp.peach.apple.pit.config

import net.tjalp.peach.peel.config.Configurable
import net.tjalp.peach.peel.config.PumpkinDetails
import net.tjalp.peach.peel.config.RedisDetails

abstract class AppleConfig : Configurable {

    /**
     * The pumpkin details
     */
    var pumpkin: PumpkinDetails = PumpkinDetails()

    /**
     * Redis connection instructions
     */
    var redis: RedisDetails = RedisDetails()

    /**
     * The current node's port
     */
    var port: Int = 25000
}