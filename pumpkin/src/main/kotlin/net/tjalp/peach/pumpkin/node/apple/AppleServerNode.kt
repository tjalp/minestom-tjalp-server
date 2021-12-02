package net.tjalp.peach.pumpkin.node.apple

import net.tjalp.peach.proto.apple.Apple.AppleHealthReport
import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.DockerNode
import net.tjalp.peach.pumpkin.node.HealthMonitor
import net.tjalp.peach.pumpkin.player.ConnectedPlayer
import net.tjalp.peach.pumpkin.player.PeachPlayer

class AppleServerNode(
    private val pumpkin: PumpkinServer,
    override val dockerNode: DockerNode,
    override val nodeIdentifier: String,
    override val server: String,
    override val port: Int
) : AppleNode {

    override val healthMonitor: HealthMonitor<AppleHealthReport> = HealthMonitor(this)
    override val players: List<PeachPlayer>
        get() = connectedPlayers
    override val playerCount: Int
        get() = players.size
    override val isOnline: Boolean
        get() = healthMonitor.isOnline

    internal val connectedPlayers = ArrayList<ConnectedPlayer>()

    init {

        // The main thread
        val thread = pumpkin.mainThread

        // Unregister everything when the connection is dropped
        healthMonitor.onConnectionDrop
            .publishOn(thread)
            .subscribe {
                pumpkin.nodeService.unregister(this)
            }
    }

    override fun connect(player: PeachPlayer) {
        pumpkin.playerService.switch(player, this)
    }

    override fun dispose() {
        healthMonitor.close()
    }
}