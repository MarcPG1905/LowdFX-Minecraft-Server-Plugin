package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.TeleportManager;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public final class TpCommands {
    public static LiteralCommandNode<CommandSourceStack> tphereCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("tphere")
                .requires(source -> Perms.check(source, Perms.Perm.TPHERE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                        .executes(context -> {
                            Location target = context.getSource().getLocation();
                            Component message = LowdFX.serverMessage(Component.text("Du wurdest zu " + context.getSource().getSender().getName() + " teleportiert!", NamedTextColor.GREEN));
                            context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).forEach(p -> {
                                TeleportManager.teleportSafe(p, target);
                                p.sendMessage(message);
                            });
                            return 1;
                        })
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> tpallCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("tpall")
                .requires(source -> Perms.check(source, Perms.Perm.TPALL))
                .executes(context -> {
                    Location target = context.getSource().getLocation();
                    Component message = LowdFX.serverMessage(Component.text("Du wurdest zu " + context.getSource().getSender().getName() + " teleportiert!", NamedTextColor.GREEN));
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        TeleportManager.teleportSafe(p, target);
                        p.sendMessage(message);
                    });
                    return 1;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> backCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("back")
                .requires(source -> Perms.check(source, Perms.Perm.BACK))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
                        return 1;
                    }

                    Location target = TeleportManager.backPoint(player.getUniqueId());
                    if (target == null) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst nicht zurück teleportieren!", NamedTextColor.RED)));
                        return 1;
                    }

                    TeleportManager.teleportSafe(player, target);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest zurück teleportiert!", NamedTextColor.GREEN)));
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                        .executes(context -> {
                            Collection<Player> players = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                            for (Player p : players) {
                                Location target = TeleportManager.backPoint(p.getUniqueId());
                                if (target == null) {
                                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(p.getName() + " kann nicht zurück teleportieren!", NamedTextColor.RED)));
                                    return 1;
                                }
                                TeleportManager.teleportSafe(p, target);
                                p.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest zurück teleportiert!", NamedTextColor.GREEN)));
                            }

                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler wurden zurück teleportiert!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .build();
    }

    // Didn't add normal TP, because it just doesn't make any sense, as it's a vanilla command anyways.
    // Also, tphere is just `/tp [player] @s` and tpall is just `/tp @a @s`.
    // Not sure why you need some special commands for basically any of these.
}
