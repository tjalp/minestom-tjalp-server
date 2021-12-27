package net.tjalp.peach.peel.config

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