package net.tjalp.peach.pumpkin.node.melon

import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.HealthMonitor
import net.tjalp.peach.pumpkin.player.PeachPlayer

class MelonServerNode(
    private val pumpkin: PumpkinServer,
    override val nodeId: String
) : MelonNode {

    override val healthMonitor: HealthMonitor
        get() = TODO("Not yet implemented")
    override val players: List<PeachPlayer>
        get() = connectedPlayers
    override val playerCount: Int
        get() = TODO("Not yet implemented")
    override val isOnline: Boolean
        get() = TODO("Not yet implemented")

    internal val connectedPlayers = ArrayList<PeachPlayer>()

    override fun dispose() {
        TODO("Not yet implemented")
    }
}