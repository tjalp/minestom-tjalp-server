package net.tjalp.peach.apple.green.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

public class SkinCommand extends Command {

    public SkinCommand() {
        super("skin", "setskin");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Component.text("Invalid usage!").color(NamedTextColor.RED));
        });

        var username = ArgumentType.String("username");
        var targets = ArgumentType.Entity("targets").singleEntity(false).onlyPlayers(true);
        var reset = ArgumentType.Literal("reset");
        var set = ArgumentType.Literal("set");
        var to = ArgumentType.Literal("to");

        addSyntax(this::executeUsername, set, username);
        addSyntax(this::executeUsernameTargets, set, targets, to, username);
        addSyntax(this::executeReset, reset);
        addSyntax(this::executeReset, reset, targets);
    }

    private void executeUsername(CommandSender sender, CommandContext context) {
        String username = context.get("username");

        if (!sender.isPlayer()) {
            sender.sendMessage(Component.text("A player is required").color(NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;

        player.setSkin(PlayerSkin.fromUsername(username));
    }

    private void executeUsernameTargets(CommandSender sender, CommandContext context) {
        String username = context.get("username");
        EntityFinder finder = context.get("targets");

        List<Entity> playerList = finder.find(sender);

        if (playerList.isEmpty()) {
            sender.sendMessage(Component.text("No players were found").color(NamedTextColor.RED));
            return;
        }

        PlayerSkin skin = PlayerSkin.fromUsername(username);

        playerList.forEach(entity -> ((Player) entity).setSkin(skin));
    }

    private void executeReset(CommandSender sender, CommandContext context) {
        EntityFinder finder = context.get("targets");

        if (finder == null) {
            if (!sender.isPlayer()) {
                sender.sendMessage(Component.text("No targets were found").color(NamedTextColor.RED));
                return;
            }
            ((Player) sender).setSkin(PlayerSkin.fromUsername(((Player) sender).getUsername()));
            return;
        }

        List<Entity> targets = finder.find(sender);

        targets.forEach(target -> {
            Player targetPlayer = (Player) target;

            targetPlayer.setSkin(PlayerSkin.fromUsername(targetPlayer.getUsername()));
        });
    }
}
