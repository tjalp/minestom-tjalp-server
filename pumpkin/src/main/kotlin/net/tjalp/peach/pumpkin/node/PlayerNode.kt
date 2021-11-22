package net.tjalp.peach.pumpkin.node

import net.tjalp.peach.pumpkin.player.PeachPlayer

/**
 * A [Node] that contains players
 */
interface PlayerNode : Node {

    /**
     * list of players connected to this node
     */
    val players: List<PeachPlayer>

    /**
     * The amount of players connected to this node
     */
    val playerCount: Int

    override fun compareTo(other: Node): Int {
        if(other !is PlayerNode) return 0
        return this.playerCount - other.playerCount
    }

}