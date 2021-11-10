package net.tjalp.peach.apple.green.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.utils.entity.EntityFinder
import net.minestom.server.utils.location.RelativeVec
import java.util.*
import java.util.function.Consumer

class TeleportCommand : Command("teleport", "tp") {

    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage(Component.text("Invalid usage!").color(NamedTextColor.RED))
        }

        val destination = ArgumentType.Entity("destination").singleEntity(true).onlyPlayers(false)
        val location = ArgumentType.RelativeVec3("location")
        val targets = ArgumentType.Entity("targets").singleEntity(false).onlyPlayers(false)

        addSyntax(this::executeDestination, destination)
        addSyntax(this::executeLocation, location)
        addSyntax(this::executeTargetsDestination, targets, destination)
        addSyntax(this::executeTargetsLocation, targets, location)
    }

    private fun executeDestination(sender: CommandSender, context: CommandContext) {
        val destination = context.get<EntityFinder>("destination")

        if (sender !is Player) {
            sender.sendMessage(Component.text("A player is required").color(NamedTextColor.RED))
            return
        }

        val entity = destination.findFirstEntity(sender)

        if (entity == null) {
            sender.sendMessage(Component.text("No entity was found").color(NamedTextColor.RED))
            return
        }

        val name = if (entity is Player) {
            entity.name
        } else if (entity.entityMeta.customName != null) {
            entity.entityMeta.customName
        } else {
            Component.translatable(entity.entityType.registry().translationKey())
        }

        sender.teleport(entity.position).whenComplete { _, _ ->
            sender.sendMessage(
                Component.translatable(
                    "commands.teleport.success.entity.single",
                    sender.name,
                    name
                )
            )
        }
    }

    private fun executeLocation(sender: CommandSender, context: CommandContext) {
        val destination = context.get<RelativeVec>("location")

        if (sender !is Player) {
            sender.sendMessage(Component.text("A player is required").color(NamedTextColor.RED))
            return
        }

        val position = destination.from(sender).asPosition().withPitch(sender.position.pitch()).withYaw(sender.position.yaw())

        sender.teleport(position).whenComplete { _, _ ->
            sender.sendMessage(
                Component.translatable(
                    "commands.teleport.success.location.single",
                    sender.name,
                    Component.text(String.format(Locale.ROOT, "%f", position.x())),
                    Component.text(String.format(Locale.ROOT, "%f", position.y())),
                    Component.text(String.format(Locale.ROOT, "%f", position.z()))
                )
            )
        }
    }

    private fun executeTargetsDestination(sender: CommandSender, context: CommandContext) {
        val targets = context.get<EntityFinder>("targets")
        val destination = context.get<EntityFinder>("destination")
        val entity = destination.findFirstEntity(sender)

        if (entity == null) {
            sender.sendMessage(Component.text("No entity was found").color(NamedTextColor.RED))
            return
        }

        val entityList = targets.find(sender)
        entityList.forEach { it.teleport(entity.position) }

        val name = if (entity is Player) {
            entity.name
        } else if (entity.entityMeta.customName != null) {
            entity.entityMeta.customName
        } else {
            Component.translatable(entity.entityType.registry().translationKey())
        }

        sender.sendMessage(
            Component.translatable(
                "commands.teleport.success.entity.multiple",
                Component.text(entityList.size),
                name
            )
        )
    }

    private fun executeTargetsLocation(sender: CommandSender, context: CommandContext) {
        val targets = context.get<EntityFinder>("targets")
        val location = context.get<RelativeVec>("location")
        val entityList = targets.find(sender)
        val position = location.fromSender(sender).asPosition()

        entityList.forEach { it.teleport(position) }

        sender.sendMessage(
            Component.translatable(
                "commands.teleport.success.location.multiple",
                Component.text(entityList.size),
                Component.text(String.format(Locale.ROOT, "%f", position.x())),
                Component.text(String.format(Locale.ROOT, "%f", position.y())),
                Component.text(String.format(Locale.ROOT, "%f", position.z()))
            )
        )
    }
}