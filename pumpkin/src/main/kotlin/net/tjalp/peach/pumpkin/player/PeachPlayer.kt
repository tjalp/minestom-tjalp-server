package net.tjalp.peach.pumpkin.player

import net.tjalp.peach.pumpkin.node.apple.AppleNode
import net.tjalp.peach.pumpkin.node.melon.MelonNode
import java.util.*

/**
 * Represents a player on the network
 */
interface PeachPlayer {

    /**
     * The player's unique identifier
     */
    val uniqueId: UUID

    /**
     * The player's username
     */
    val username: String

    /**
     * The current melon node the player
     * is connected to
     */
    val currentMelonNode: MelonNode

    /**
     * The current apple node the player
     * is connected to
     */
    val currentAppleNode: AppleNode
}