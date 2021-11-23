package net.tjalp.peach.apple.green.listener

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.event.EventNode
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.item.PickupItemEvent
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerCommandEvent
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.event.player.PlayerSkinInitEvent
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.extras.bungee.BungeeCordProxy
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.EffectPacket
import net.minestom.server.utils.PacketUtils
import net.minestom.server.utils.time.TimeUnit
import net.tjalp.peach.apple.green.MinestomAppleServer
import net.tjalp.peach.proto.apple.Apple

/**
 * The main event listener
 */
class AppleEventListener(
    val server: MinestomAppleServer
) {

    init {
        val node = EventNode.all("apple")

        MinecraftServer.getGlobalEventHandler().addChild(node)

        node.addListener(ServerListPingEvent::class.java, this::onServerListPing)
        node.addListener(PlayerLoginEvent::class.java, this::onPlayerLogin)
        node.addListener(PlayerBlockBreakEvent::class.java, this::onPlayerBlockBreak)
        node.addListener(ItemDropEvent::class.java, this::onItemDrop)
        node.addListener(PickupItemEvent::class.java, this::onPickupItem)
        node.addListener(PlayerCommandEvent::class.java, this::onPlayerCommand)
        node.addListener(PlayerSkinInitEvent::class.java, this::onPlayerSkinInit)
    }

    private fun onServerListPing(event: ServerListPingEvent) {
        val data = event.responseData

        data.protocol = -1
        data.version = LegacyComponentSerializer.legacySection().serialize(
            Component.text("tjalp's ").color(NamedTextColor.DARK_AQUA)
                .append(Component.text("server ->").color(NamedTextColor.AQUA))
                .append(Component.text("                                                                    "))
                .append(Component.text(data.online).color(NamedTextColor.GRAY))
                .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(data.maxPlayer).color(NamedTextColor.GRAY))
        )
    }

    private fun onPlayerLogin(event: PlayerLoginEvent) {
        val player = event.player

        event.setSpawningInstance(server.overworld)
        player.respawnPoint = Pos(0.5, 64.0, 0.5)
        player.permissionLevel = 4

        val request = Apple.PlayerHandshakeRequest.newBuilder()
            .setUuid(player.uuid.toString())
            .setPlayerName(player.username)
            .build()

        GlobalScope.async {
            server.rpcStub.playerHandshake(request)
        }
    }

    private fun onPlayerBlockBreak(event: PlayerBlockBreakEvent) {
        val effectPacket: ServerPacket = EffectPacket(
            2001,
            event.blockPosition,
            event.block.stateId().toInt(),
            false
        )
        val chunk = event.instance.getChunkAt(event.blockPosition) ?: return

        PacketUtils.sendGroupedPacket(
            chunk.viewers, effectPacket
        ) { viewer: Player -> viewer != event.player }
    }

    private fun onItemDrop(event: ItemDropEvent) {
        val player = event.player
        val droppedItem = event.itemStack

        if (player.instance == null) return

        val itemEntity = ItemEntity(droppedItem)
        itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND)
        itemEntity.setInstance(player.instance!!)
        itemEntity.spawn()
        itemEntity.teleport(player.position.add(0.0, 1.5, 0.0))

        val velocity = player.position.direction().mul(6.0)
        itemEntity.velocity = velocity
    }

    private fun onPickupItem(event: PickupItemEvent) {
        if (event.entity !is Player) return

        val couldAdd = (event.entity as Player).inventory.addItemStack(event.itemStack)
        event.isCancelled = !couldAdd
    }

    private fun onPlayerCommand(event: PlayerCommandEvent) {
        val player = event.player
        val man = MinecraftServer.getCommandManager()

        if (!man.commandExists(event.command.split(" ")[0])) {
            player.sendMessage(Component.text("That command does not exist!").color(NamedTextColor.RED))
            event.isCancelled = true
        }
    }

    private fun onPlayerSkinInit(event: PlayerSkinInitEvent) {
        if (!MojangAuth.isEnabled() && !VelocityProxy.isEnabled() && !BungeeCordProxy.isEnabled()) event.skin = PlayerSkin.fromUsername(event.player.username)
    }
}