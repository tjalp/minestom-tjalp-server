package net.tjalp.peach.melon.config

import net.tjalp.peach.peel.config.Configurable
import net.tjalp.peach.peel.config.PumpkinDetails
import net.tjalp.peach.peel.config.RedisDetails

class MelonConfig : Configurable {

    /** The pumpkin details **/
    var pumpkin: PumpkinDetails = PumpkinDetails()

    /**
     * The redis details
     */
    var redis: RedisDetails = RedisDetails()
}