package net.tjalp.peach.pumpkin.config

import net.tjalp.peach.peel.config.Configurable
import net.tjalp.peach.peel.config.DockerDetails
import net.tjalp.peach.peel.config.RedisDetails

class PumpkinConfig : Configurable {

    /**
     * The port Pumpkin should listen on
     */
    var port: Int = 34040

    /**
     * Redis connection instructions
     */
    var redis = RedisDetails()

    /**
     * A list of all docker nodes that can be used
     */
    var dockerNodes: List<DockerDetails> = ArrayList()
}