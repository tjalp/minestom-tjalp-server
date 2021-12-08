package net.tjalp.peach.apple.green.command

import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.tjalp.peach.apple.green.MinestomAppleServer
import net.tjalp.peach.apple.pit.command.NODE_ID
import net.tjalp.peach.apple.pit.command.NODE_TYPE
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
        val stop = ArgumentType.Literal("stop")
        val kill = ArgumentType.Literal("kill")
        val nodeType = ArgumentType.String(NODE_TYPE)
        val nodeId = ArgumentType.String(NODE_ID)

        addSyntax(this::executeNodeCreate, node, create, nodeType)
        addSyntax(this::executeNodeCreate, node, create, nodeType, nodeId)
        addSyntax(this::executeNodeStop, node, stop, nodeId)
        addSyntax(this::executeNodeKill, node, kill, nodeId)
    }

    private fun executeNodeCreate(sender: CommandSender, context: CommandContext) {
        val nodeType = context.get<String>(NODE_TYPE)
        val nodeId = context.get<String>(NODE_ID)

        sender.sendMessage(Component.text("Creating node...").color(NamedTextColor.YELLOW))

        apple.scheduler.launch {
            val request = Apple.CreateNodeRequest.newBuilder()
                .setNodeType(nodeType)

            if (nodeId != null) request.nodeIdentifier = nodeId

            val response = apple.rpcStub.createNode(request.build())

            if (response.success) {
                sender.sendMessage(
                    Component.text("Succesfully created node with type ").color(NamedTextColor.GREEN)
                        .append(Component.text(response.nodeType).color(NamedTextColor.DARK_GREEN))
                        .append(Component.text(" and identifier "))
                        .append(Component.text(response.nodeIdentifier)
                            .color(NamedTextColor.DARK_GREEN)
                            .hoverEvent(HoverEvent.showText(Component.text("Click to copy!").color(NamedTextColor.GREEN)))
                            .clickEvent(ClickEvent.copyToClipboard(response.nodeIdentifier)))
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

    private fun executeNodeStop(sender: CommandSender, context: CommandContext) {
        val nodeId = context.get<String>(NODE_ID)

        sender.sendMessage(
            Component.text("Stopping node with identifier ").color(NamedTextColor.YELLOW)
                .append(Component.text(nodeId).color(NamedTextColor.GOLD))
        )

        apple.scheduler.launch {
            val request = Apple.StopNodeRequest.newBuilder()
                .setNodeIdentifier(nodeId)
                .build()

            apple.rpcStub.stopNode(request)

            sender.sendMessage(Component.text("Succesfully stopped node with identifier ").color(NamedTextColor.GREEN)
                .append(Component.text(nodeId).color(NamedTextColor.DARK_GREEN)))
        }
    }

    private fun executeNodeKill(sender: CommandSender, context: CommandContext) {
        val nodeId = context.get<String>(NODE_ID)

        sender.sendMessage(
            Component.text("Killing node with identifier ").color(NamedTextColor.YELLOW)
                .append(Component.text(nodeId).color(NamedTextColor.GOLD))
        )

        apple.scheduler.launch {
            val request = Apple.KillNodeRequest.newBuilder()
                .setNodeIdentifier(nodeId)
                .build()

            apple.rpcStub.killNode(request)

            sender.sendMessage(Component.text("Succesfully killed node with identifier ").color(NamedTextColor.GREEN)
                .append(Component.text(nodeId).color(NamedTextColor.DARK_GREEN)))
        }
    }
}