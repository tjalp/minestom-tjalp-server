package net.tjalp.peach.apple.pit.config

import net.tjalp.peach.peel.config.Configurable
import net.tjalp.peach.peel.config.RedisDetails

abstract class AppleConfig : Configurable {

    /**
     * Redis connection instructions
     */
    var redis: RedisDetails = RedisDetails()
}