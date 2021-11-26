package net.tjalp.peach.pumpkin.player

import net.tjalp.peach.peel.PLAYER_SWITCH
import net.tjalp.peach.peel.signal.PlayerSwitchSignal
import net.tjalp.peach.pumpkin.PumpkinServer
import net.tjalp.peach.pumpkin.node.apple.AppleNode
import net.tjalp.peach.pumpkin.node.apple.AppleServerNode
import net.tjalp.peach.pumpkin.node.melon.MelonNode
import net.tjalp.peach.pumpkin.node.melon.MelonServerNode
import reactor.core.publisher.Mono
import java.util.*

class PlayerService(
    val pumpkin: PumpkinServer
) {

    private val thread = pumpkin.mainThread

    private val registeredPlayers = mutableListOf<PeachPlayer>()

    fun setup() {
        pumpkin.logger.info("Setting up player service")
    }

    fun switch(player: PeachPlayer, node: AppleNode) {
        thread.ensureMainThread()

        if (!node.isOnline) {
            throw IllegalStateException("Target Apple Node is not online!")
        } else {
            if (player.currentAppleNode != node) {

                (player.currentAppleNode as AppleServerNode).connectedPlayers.remove(player)
                (node as AppleServerNode).connectedPlayers.add(player as ConnectedPlayer)

                player.currentAppleNode = node

                val signal = PlayerSwitchSignal().apply {
                    this.uniqueId = player.uniqueId
                    this.nodeId = node.nodeIdentifier
                }

                pumpkin.redis.publish(PLAYER_SWITCH, signal).subscribe()
            }
        }
    }

    fun getPlayer(uniqueId: UUID): PeachPlayer? {
        thread.ensureMainThread()
        return registeredPlayers.firstOrNull {
            it.uniqueId == uniqueId
        }
    }

    fun getPlayers(username: String): List<PeachPlayer> {
        thread.ensureMainThread()
        return registeredPlayers.filter {
            it.username == username
        }
    }

    internal fun register(
        uniqueId: UUID,
        username: String,
        melonNode: MelonNode,
        appleNode: AppleNode
    ): PeachPlayer {
        thread.ensureMainThread()

        if (registeredPlayers.any { it.uniqueId == uniqueId }) {
            throw IllegalArgumentException("A player with the unique identifier $uniqueId is already registered!")
        }

        val connectedPlayer = ConnectedPlayer(uniqueId, username, melonNode, appleNode)
        val melonServerNode = melonNode as MelonServerNode
        val appleServerNode = appleNode as AppleServerNode

        melonServerNode.connectedPlayers.add(connectedPlayer)
        appleServerNode.connectedPlayers.add(connectedPlayer)

        registeredPlayers.add(connectedPlayer)

        pumpkin.logger.info("Registered player (username: $username, uniqueId: $uniqueId)")

        return connectedPlayer
    }

    internal fun unregister(player: ConnectedPlayer) {
        thread.ensureMainThread()

        if (player !in registeredPlayers) {
            throw IllegalArgumentException("Unknown player $player")
        }

        val melonServerNode = player.currentMelonNode as MelonServerNode
        val appleServerNode = player.currentAppleNode as AppleServerNode

        melonServerNode.connectedPlayers.remove(player)
        appleServerNode.connectedPlayers.remove(player)

        registeredPlayers.remove(player)

        pumpkin.logger.info("Unregistered player (username: ${player.username}, uniqueId: ${player.uniqueId})")
    }
}