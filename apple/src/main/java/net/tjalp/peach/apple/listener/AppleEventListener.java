package net.tjalp.peach.apple.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Chunk;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.tjalp.peach.apple.AppleServer;

/**
 * The main event listener
 */
public class AppleEventListener {

    public AppleEventListener() {
        EventNode<Event> node = EventNode.all("backend");

        MinecraftServer.getGlobalEventHandler().addChild(node);

        node.addListener(ServerListPingEvent.class, this::onServerListPing);
        node.addListener(PlayerLoginEvent.class, this::onPlayerLogin);
        node.addListener(PlayerBlockBreakEvent.class, this::onPlayerBlockBreak);
        node.addListener(PlayerBlockPlaceEvent.class, this::onPlayerBlockPlace);
        node.addListener(ItemDropEvent.class, this::onItemDrop);
        node.addListener(PickupItemEvent.class, this::onPickupItem);
        node.addListener(PlayerCommandEvent.class, this::onCommand);
        node.addListener(PlayerSkinInitEvent.class, this::onPlayerSkinInit);
    }

    private void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        event.setSpawningInstance(AppleServer.get().overworld);
        player.setRespawnPoint(new Pos(0.5, 64, 0.5));
        player.setPermissionLevel(4);
    }

    private void onServerListPing(ServerListPingEvent event) {
        ResponseData data = event.getResponseData();

        data.setProtocol(-1);
        data.setVersion(
                LegacyComponentSerializer.legacySection().serialize(
                        Component.text("tjalp's ").color(NamedTextColor.DARK_AQUA)
                                .append(Component.text("server ->").color(NamedTextColor.AQUA))
                                .append(Component.text("                                                                    "))
                                .append(Component.text(data.getOnline()).color(NamedTextColor.GRAY))
                                .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
                                .append(Component.text(data.getMaxPlayer()).color(NamedTextColor.GRAY))
                )
        );
    }

    private void onPlayerBlockBreak(PlayerBlockBreakEvent event) {
        ServerPacket packet = new EffectPacket(
                2001,
                event.getBlockPosition(),
                event.getBlock().stateId(),
                false
        );

        Chunk chunk = event.getInstance().getChunkAt(event.getBlockPosition());

        PacketUtils.sendGroupedPacket(chunk.getViewers(), packet, viewer -> !viewer.equals(event.getPlayer()));
    }

    private void onPlayerBlockPlace(PlayerBlockPlaceEvent event) {
//        ServerPacket packet = new SoundEffectPacket(
//                Sound.Source.BLOCK,
//
//        );
//
//        Chunk chunk = event.getInstance().getChunkAt(event.getBlockPosition());
//
//        PacketUtils.sendGroupedPacket(chunk.getViewers(), packet, viewer -> !viewer.equals(event.getPlayer()));
    }

    private void onItemDrop(ItemDropEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemStack();

        if (player.getInstance() == null) return;

        ItemEntity itemEntity = new ItemEntity(droppedItem);
        itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
        itemEntity.setInstance(player.getInstance());
        itemEntity.spawn();
        itemEntity.teleport(player.getPosition().add(0, 1.5f, 0));

        Vec velocity = player.getPosition().direction().mul(6);
        itemEntity.setVelocity(velocity);
    }

    private void onPickupItem(PickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        boolean couldAdd = player.getInventory().addItemStack(event.getItemStack());
        event.setCancelled(!couldAdd);
    }

    private void onCommand(PlayerCommandEvent event) {
        Player player = event.getPlayer();
        CommandManager man = MinecraftServer.getCommandManager();

        if (!man.commandExists(event.getCommand().split(" ")[0])) {
            player.sendMessage(Component.text("That command does not exist!").color(NamedTextColor.RED));
            event.setCancelled(true);
        }
    }

    private void onPlayerSkinInit(PlayerSkinInitEvent event) {
        if (!VelocityProxy.isEnabled()) event.setSkin(PlayerSkin.fromUsername(event.getPlayer().getUsername()));
    }
}
