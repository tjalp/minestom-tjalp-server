package net.tjalp.peach.pumpkin.node.apple

import net.tjalp.peach.pumpkin.player.PeachPlayer

class AppleServerNode : AppleNode {

    override val players: List<PeachPlayer>
        get() = connectedPlayers
    override val playerCount: Int
        get() = TODO("Not yet implemented")
    override val nodeId: String
        get() = TODO("Not yet implemented")
    override val isOnline: Boolean
        get() = TODO("Not yet implemented")

    internal val connectedPlayers = ArrayList<PeachPlayer>()

    override fun dispose() {
        TODO("Not yet implemented")
    }
}