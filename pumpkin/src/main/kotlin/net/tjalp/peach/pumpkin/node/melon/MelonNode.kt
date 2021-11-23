package net.tjalp.peach.pumpkin.node.melon

import net.tjalp.peach.proto.melon.Melon.MelonHealthReport
import net.tjalp.peach.pumpkin.node.HealthMonitor
import net.tjalp.peach.pumpkin.node.PlayerNode

interface MelonNode : PlayerNode {

    /**
     * The health monitor instance
     */
    val healthMonitor: HealthMonitor<MelonHealthReport>
}