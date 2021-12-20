package net.tjalp.peach.apple.green.command

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.tjalp.peach.apple.green.MinestomAppleServer
import net.tjalp.peach.apple.pit.command.NODE_ID
import net.tjalp.peach.apple.pit.command.NODE_PORT
import net.tjalp.peach.apple.pit.command.NODE_TYPE
import net.tjalp.peach.peel.node.NodeType
import net.tjalp.peach.proto.apple.Apple

class PeachCommand(
    val apple: MinestomAppleServer
) : Command("peach") {

    private lateinit var nodeListCache: List<Apple.NodeInfo>
    private var lastCacheTime = 0L

    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage(Component.text("Invalid usage!").color(NamedTextColor.RED))
        }

        val node = ArgumentType.Literal("node")
        val create = ArgumentType.Literal("create")
        val stop = ArgumentType.Literal("stop")
        val kill = ArgumentType.Literal("kill")
        val nodeType = ArgumentType.Enum(NODE_TYPE, NodeType::class.java).setFormat(ArgumentEnum.Format.LOWER_CASED)
        val nodeId = ArgumentType.String(NODE_ID).setDefaultValue(null)
        val nodePort = ArgumentType.Integer(NODE_PORT).setDefaultValue(null)

        // TODO The next few lines can probably be improved if ArgumentLoop in Minestom is fixed.
        // You can currently only create loops with a forced order, which is inconvenient and removes the entire point.
        addSyntax(this::executeNodeCreate, node, create, nodeType)
        addSyntax(this::executeNodeCreate, node, create, nodeType, nodeId)
        addSyntax(this::executeNodeCreate, node, create, nodeType, nodeId, nodePort)
        addSyntax(this::executeNodeStop, node, stop, nodeId.setSuggestionCallback(this::suggestNodeIdList))
        addSyntax(this::executeNodeKill, node, kill, nodeId.setSuggestionCallback(this::suggestNodeIdList))
    }

    private fun executeNodeCreate(sender: CommandSender, context: CommandContext) {
        val nodeType = context.get<NodeType>(NODE_TYPE)
        val nodeId = context.get<String>(NODE_ID)
        val nodePort = context.get<Int>(NODE_PORT)

        sender.sendMessage(Component.text("Creating node...").color(NamedTextColor.YELLOW))

        apple.scheduler.launch {
            val request = Apple.CreateNodeRequest.newBuilder()
                .setNodeType(nodeType.name)

            if (nodeId != null) request.nodeIdentifier = nodeId
            if (nodePort != null) request.nodePort = nodePort

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

    private fun suggestNodeIdList(sender: CommandSender, context: CommandContext, suggestion: Suggestion) {
        val input = suggestion.input.lowercase()

        if (System.currentTimeMillis() - lastCacheTime >= 5000) {
            runBlocking {
                nodeListCache = apple.fetchNodes().sortedBy {
                    it.nodeIdentifier
                }
                lastCacheTime = System.currentTimeMillis()
            }
        }

        for (info in nodeListCache) {
            val nodeId = info.nodeIdentifier

            if (nodeId.lowercase().startsWith(input)) {
                suggestion.addEntry(SuggestionEntry(nodeId))
            }
        }
    }
}