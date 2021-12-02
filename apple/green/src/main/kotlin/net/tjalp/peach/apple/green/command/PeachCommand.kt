package net.tjalp.peach.apple.green.command

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.tjalp.peach.apple.green.MinestomAppleServer
import net.tjalp.peach.proto.apple.Apple

class PeachCommand(
    val apple: MinestomAppleServer
) : Command("peach") {

    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage(Component.text("Invalid usage!").color(NamedTextColor.RED))
        }

        val node = ArgumentType.Literal("node")
        val create = ArgumentType.Literal("create")
        val kill = ArgumentType.Literal("kill")
        val nodeType = ArgumentType.String("node type")
        val nodeId = ArgumentType.String("node identifier")

        addSyntax(this::executeNodeCreate, node, create, nodeType)
        addSyntax(this::executeNodeCreate, node, create, nodeType, nodeId)
        addSyntax(this::executeNodeKill, node, kill, nodeId)
    }

    private fun executeNodeCreate(sender: CommandSender, context: CommandContext) {
        val nodeType = context.get<String>("node type")
        val nodeId = context.get<String>("node identifier")

        sender.sendMessage(Component.text("Creating node...").color(NamedTextColor.YELLOW))

        GlobalScope.launch {
            val request = Apple.CreateNodeRequest.newBuilder()
                .setNodeType(nodeType)

            if (nodeId != null) request.nodeIdentifier = nodeId

            val response = apple.rpcStub.createNode(request.build())

            if (response.success) {
                sender.sendMessage(
                    Component.text("Succesfully created node with type ").color(NamedTextColor.GREEN)
                        .append(Component.text(response.nodeType).color(NamedTextColor.DARK_GREEN))
                        .append(Component.text(" and identifier "))
                        .append(Component.text(response.nodeIdentifier).color(NamedTextColor.DARK_GREEN))
                )
            } else {
                sender.sendMessage(
                    Component.text("Failed to create node with type ").color(NamedTextColor.RED)
                        .append(Component.text(response.nodeType).color(NamedTextColor.DARK_RED))
                        .append(Component.text(" and identifier "))
                        .append(Component.text(response.nodeIdentifier).color(NamedTextColor.DARK_RED))
                )
            }
        }
    }

    private fun executeNodeKill(sender: CommandSender, context: CommandContext) {
        val nodeId = context.get<String>("node identifier")

        sender.sendMessage(
            Component.text("Killing node with identifier ").color(NamedTextColor.YELLOW)
                .append(Component.text(nodeId).color(NamedTextColor.GOLD))
        )

        GlobalScope.launch {
            val request = Apple.KillNodeRequest.newBuilder()
                .setNodeIdentifier(nodeId)
                .build()

            apple.rpcStub.killNode(request)

            sender.sendMessage(Component.text("Succesfully killed node with identifier ").color(NamedTextColor.GREEN)
                .append(Component.text(nodeId).color(NamedTextColor.DARK_GREEN)))
        }
    }
}