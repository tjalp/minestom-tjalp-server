package net.tjalp.peach.pumpkin.player

import net.tjalp.peach.pumpkin.node.apple.AppleNode
import net.tjalp.peach.pumpkin.node.melon.MelonNode
import java.util.*

class ConnectedPlayer(
    override val uniqueId: UUID,
    override val username: String,
    override val currentMelonNode: MelonNode,
    initialAppleNode: AppleNode
) : PeachPlayer {

    override var currentAppleNode: AppleNode = initialAppleNode
}