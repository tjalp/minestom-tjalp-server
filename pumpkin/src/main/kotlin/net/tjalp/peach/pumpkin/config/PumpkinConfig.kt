package net.tjalp.peach.pumpkin.config

import net.tjalp.peach.peel.config.Configurable
import net.tjalp.peach.peel.config.DockerDetails
import net.tjalp.peach.peel.config.PumpkinDetails
import net.tjalp.peach.peel.config.RedisDetails

class PumpkinConfig : Configurable {

    /**
     * The pumpkin details
     */
    var pumpkin = PumpkinDetails()

    /**
     * Redis connection instructions
     */
    var redis = RedisDetails()

    /**
     * A list of all docker nodes that can be used
     */
    var dockerNodes: List<DockerDetails> = ArrayList()
}