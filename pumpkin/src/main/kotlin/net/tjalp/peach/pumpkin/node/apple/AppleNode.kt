package net.tjalp.peach.pumpkin.node.apple

import net.tjalp.peach.proto.apple.Apple.AppleHealthReport
import net.tjalp.peach.pumpkin.node.HealthMonitor
import net.tjalp.peach.pumpkin.node.PlayerNode

interface AppleNode : PlayerNode {

    /**
     * The health monitor instance
     */
    val healthMonitor: HealthMonitor<AppleHealthReport>

    /**
     * The apple node's server
     */
    val server: String

    /**
     * The apple node's port
     */
    val port: Int
}