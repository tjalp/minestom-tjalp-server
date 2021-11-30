package net.tjalp.peach.peel.config

class MelonConfig : NodeConfig() {

    /** The pumpkin details **/
    var pumpkin: PumpkinDetails = PumpkinDetails()

    /**
     * The redis details
     */
    var redis: RedisDetails = RedisDetails()
}