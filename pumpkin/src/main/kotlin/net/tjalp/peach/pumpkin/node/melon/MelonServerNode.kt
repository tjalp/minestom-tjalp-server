package net.tjalp.peach.pumpkin.node.melon

import net.tjalp.peach.proto.melon.Melon
import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.DockerNode
import net.tjalp.peach.pumpkin.node.HealthMonitor
import net.tjalp.peach.pumpkin.player.ConnectedPlayer
import net.tjalp.peach.pumpkin.player.PeachPlayer

class MelonServerNode(
    private val pumpkin: PumpkinServer,
    override val dockerNode: DockerNode,
    override val nodeIdentifier: String
) : MelonNode {

    override val healthMonitor: HealthMonitor<Melon.MelonHealthReport> = HealthMonitor(this)
    override val players: List<PeachPlayer>
        get() = connectedPlayers
    override val playerCount: Int
        get() = players.size
    override val isOnline: Boolean
        get() = healthMonitor.isOnline

    val connectedPlayers = ArrayList<ConnectedPlayer>()

    init {

        // The main thread
        val thread = pumpkin.mainThread

        // Unregister everything when the connection is dropped
        healthMonitor.onConnectionDrop
            .publishOn(thread)
            .subscribe {
                for (player in connectedPlayers) pumpkin.playerService.unregister(player)

                pumpkin.nodeService.unregister(this)
            }
    }

    override fun dispose() {
        healthMonitor.close()
    }
}