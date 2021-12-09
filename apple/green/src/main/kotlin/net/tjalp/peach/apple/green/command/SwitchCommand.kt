package net.tjalp.peach.apple.green.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.tjalp.peach.apple.pit.AppleServer
import net.tjalp.peach.apple.pit.command.NODE_ID

class SwitchCommand : Command("switch") {

    init {
        setDefaultExecutor { sender, context ->
            sender.sendMessage(Component.text("Usage: /${context.commandName} <$NODE_ID>").color(NamedTextColor.RED))
        }

        val nodeId = ArgumentType.String(NODE_ID)

        addSyntax(this::execute, nodeId)
    }

    private fun execute(sender: CommandSender, context: CommandContext) {
        val nodeId = context.get<String>(NODE_ID)

        if (sender !is Player) {
            sender.sendMessage(Component.text("A player is required").color(NamedTextColor.RED))
            return
        }

        AppleServer.get().switchPlayer(sender.uuid, nodeId)

        sender.sendMessage("Switching to $nodeId...")
    }
}