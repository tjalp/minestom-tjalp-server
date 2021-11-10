package net.tjalp.peach.apple.green.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.utils.entity.EntityFinder
import java.util.function.Consumer

class SkinCommand : Command("skin", "setskin") {

    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage(Component.text("Invalid usage!").color(NamedTextColor.RED))
        }

        val username = ArgumentType.String("username")
        val targets = ArgumentType.Entity("targets").singleEntity(false).onlyPlayers(true)
        val reset = ArgumentType.Literal("reset")
        val set = ArgumentType.Literal("set")
        val to = ArgumentType.Literal("to")

        addSyntax(this::executeUsername, set, username)
        addSyntax(this::executeUsernameTargets, set, targets, to, username)
        addSyntax(this::executeReset, reset)
        addSyntax(this::executeReset, reset, targets)
    }

    private fun executeUsername(sender: CommandSender, context: CommandContext) {
        val username = context.get<String>("username")

        if (!sender.isPlayer) {
            sender.sendMessage(Component.text("A player is required").color(NamedTextColor.RED))
            return
        }

        val player = sender as Player

        player.skin = PlayerSkin.fromUsername(username)
    }

    private fun executeUsernameTargets(sender: CommandSender, context: CommandContext) {
        val username = context.get<String>("username")
        val finder = context.get<EntityFinder>("targets")

        val playerList = finder.find(sender)

        if (playerList.isEmpty()) {
            sender.sendMessage(Component.text("No players were found").color(NamedTextColor.RED))
            return
        }

        val skin = PlayerSkin.fromUsername(username)

        playerList.forEach {
            (it as Player).skin = skin
        }
    }

    private fun executeReset(sender: CommandSender, context: CommandContext) {
        val finder = context.get<EntityFinder>("targets")

        if (finder == null) {
            if (!sender.isPlayer) {
                sender.sendMessage(Component.text("No targets were found").color(NamedTextColor.RED))
                return
            }
            (sender as Player).skin = PlayerSkin.fromUsername(sender.username)
            return
        }

        val targets = finder.find(sender)

        targets.forEach {
            val targetPlayer = it as Player
            targetPlayer.skin = PlayerSkin.fromUsername(targetPlayer.username)
        }
    }
}