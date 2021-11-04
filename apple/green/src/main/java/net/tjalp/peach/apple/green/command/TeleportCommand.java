package net.tjalp.peach.apple.green.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;

import java.util.List;
import java.util.Locale;

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

        Component name;

        if (entity instanceof Player entityPlayer) name = entityPlayer.getName();
        else if (entity.getCustomName() != null) name = entity.getEntityMeta().getCustomName();
        else name = Component.translatable(entity.getEntityType().registry().translationKey());

        player.teleport(entity.getPosition()).whenComplete((unused, throwable) -> player.sendMessage(Component.translatable(
                "commands.teleport.success.entity.single",
                player.getName(),
                name
        )));
    }

    private void executeLocation(CommandSender sender, CommandContext context) {
        RelativeVec destination = context.get("location");

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("A player is required").color(NamedTextColor.RED));
            return;
        }

        Pos position = destination.from(player).asPosition().withPitch(player.getPosition().pitch()).withYaw(player.getPosition().yaw());
        player.teleport(position).whenComplete((unused, throwable) -> player.sendMessage(Component.translatable(
                "commands.teleport.success.location.single",
                player.getName(),
                Component.text(String.format(Locale.ROOT, "%f", position.x())),
                Component.text(String.format(Locale.ROOT, "%f", position.y())),
                Component.text(String.format(Locale.ROOT, "%f", position.z()))
        )));
    }

    private void executeTargetsDestination(CommandSender sender, CommandContext context) {
        EntityFinder targets = context.get("targets");
        EntityFinder destination = context.get("destination");
        Entity entity = destination.findFirstEntity(sender);

        if (entity == null) {
            sender.sendMessage(Component.text("No entity was found").color(NamedTextColor.RED));
            return;
        }

        List<Entity> entityList = targets.find(sender);
        entityList.forEach(target -> target.teleport(entity.getPosition()));

        Component name;

        if (entity instanceof Player entityPlayer) name = entityPlayer.getName();
        else if (entity.getCustomName() != null) name = entity.getEntityMeta().getCustomName();
        else name = Component.translatable(entity.getEntityType().registry().translationKey());

        sender.sendMessage(Component.translatable(
                "commands.teleport.success.entity.multiple",
                Component.text(entityList.size()),
                name
        ));
    }

    private void executeTargetsLocation(CommandSender sender, CommandContext context) {
        EntityFinder targets = context.get("targets");
        RelativeVec location = context.get("location");

        List<Entity> entityList = targets.find(sender);
        Pos position = location.fromSender(sender).asPosition();
        entityList.forEach(target -> target.teleport(position));

        sender.sendMessage(Component.translatable(
                "commands.teleport.success.location.multiple",
                Component.text(entityList.size()),
                Component.text(String.format(Locale.ROOT, "%f", position.x())),
                Component.text(String.format(Locale.ROOT, "%f", position.y())),
                Component.text(String.format(Locale.ROOT, "%f", position.z()))
        ));
    }
}
