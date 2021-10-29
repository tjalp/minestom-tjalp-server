package net.tjalp.minestom.backend.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;

public class TeleportCommand extends Command {

    public TeleportCommand() {
        super("teleport", "tp");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Component.text("Invalid usage!").color(NamedTextColor.RED));
        });

        ArgumentEntity destination = ArgumentType.Entity("destination").singleEntity(true).onlyPlayers(false);
        ArgumentRelativeVec3 location = ArgumentType.RelativeVec3("location");
        ArgumentEntity targets = ArgumentType.Entity("targets").singleEntity(false).onlyPlayers(false);

        addSyntax(this::executeDestination, destination);
        addSyntax(this::executeLocation, location);
        addSyntax(this::executeTargetsDestination, targets, destination);
        addSyntax(this::executeTargetsLocation, targets, location);
    }

    private void executeDestination(CommandSender sender, CommandContext context) {
        EntityFinder destination = context.get("destination");

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("A player is required").color(NamedTextColor.RED));
            return;
        }

        Entity entity = destination.findFirstEntity(player);

        if (entity == null) {
            sender.sendMessage(Component.text("No entity was found").color(NamedTextColor.RED));
            return;
        }

        player.teleport(entity.getPosition());
    }

    private void executeLocation(CommandSender sender, CommandContext context) {
        RelativeVec destination = context.get("location");

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("A player is required").color(NamedTextColor.RED));
            return;
        }

        player.teleport(destination.from(player).asPosition().withPitch(player.getPosition().pitch()).withYaw(player.getPosition().yaw()));
    }

    private void executeTargetsDestination(CommandSender sender, CommandContext context) {
        EntityFinder targets = context.get("targets");
        EntityFinder destination = context.get("destination");
        Entity entity = destination.findFirstEntity(sender);

        if (entity == null) {
            sender.sendMessage(Component.text("No entity was found").color(NamedTextColor.RED));
            return;
        }

        targets.find(sender).forEach(target -> {
            target.teleport(entity.getPosition());
        });
    }

    private void executeTargetsLocation(CommandSender sender, CommandContext context) {
        EntityFinder targets = context.get("targets");
        RelativeVec location = context.get("location");

        targets.find(sender).forEach(target -> {
            target.teleport(location.fromSender(sender).asPosition());
        });
    }
}
