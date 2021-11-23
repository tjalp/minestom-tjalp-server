package net.tjalp.peach.pumpkin.node.apple

import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.HealthMonitor
import net.tjalp.peach.pumpkin.player.ConnectedPlayer
import net.tjalp.peach.pumpkin.player.PeachPlayer

class AppleServerNode(
    private val pumpkin: PumpkinServer,
    override val nodeId: String,
    override val server: String,
    override val port: Int
) : AppleNode {

    override val healthMonitor: HealthMonitor = HealthMonitor(this)
    override val players: List<PeachPlayer>
        get() = connectedPlayers
    override val playerCount: Int
        get() = players.size
    override val isOnline: Boolean
        get() = healthMonitor.isOnline

    internal val connectedPlayers = ArrayList<ConnectedPlayer>()

    override fun dispose() {
        healthMonitor.close()
    }
}