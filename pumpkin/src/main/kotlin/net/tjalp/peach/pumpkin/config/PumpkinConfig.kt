package net.tjalp.peach.pumpkin.config

import net.tjalp.peach.peel.config.Configurable
import net.tjalp.peach.peel.config.RedisDetails

class PumpkinConfig : Configurable {

    /** Redis connection instructions  */
    var redis = RedisDetails()
}