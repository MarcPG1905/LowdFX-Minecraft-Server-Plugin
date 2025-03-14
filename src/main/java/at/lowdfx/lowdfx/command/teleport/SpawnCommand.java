package at.lowdfx.lowdfx.command.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.teleport.SpawnManager;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public final class SpawnCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("spawn")
                .requires(source -> source.getExecutor() instanceof Player && Perms.check(source, Perms.Perm.SPAWN))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                    SpawnManager.getSpawn(player).teleportSafe(player);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest zum Spawn teleportiert!", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("tp")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    SpawnManager.SPAWNS.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    String name = context.getArgument("name", String.class);

                                    if (SpawnManager.SPAWNS.containsKey(name)) {
                                        SpawnManager.getSpawn(player).teleportSafe(player);
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest zum Spawn teleportiert!", NamedTextColor.GREEN)));
                                        Utilities.positiveSound(player);
                                    } else {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Der eingegebene Spawn " + name + " existiert nicht!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set")
                        .requires(source -> Perms.check(source, Perms.Perm.SPAWN_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    SpawnManager.SPAWNS.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(RequiredArgumentBuilder.<CommandSourceStack, FinePositionResolver>argument("location", ArgumentTypes.finePosition(true))
                                        .executes(context -> {
                                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                            String name = context.getArgument("name", String.class);
                                            Location location = context.getArgument("location", FinePositionResolver.class).resolve(context.getSource()).toLocation(context.getSource().getExecutor().getWorld());

                                            SpawnManager.setSpawn(name, location);
                                            player.sendMessage(LowdFX.serverMessage(Component.text("Der Spawn " + name + " wurde gesetzt!", NamedTextColor.GREEN)));
                                            Utilities.positiveSound(player);

                                            if (SpawnManager.SPAWNS.size() == 1) {
                                                SpawnManager.setSpawn("default", Bukkit.getWorlds().getFirst().getSpawnLocation());
                                            }
                                            return 1;
                                        })
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .requires(source -> Perms.check(source, Perms.Perm.SPAWN_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    SpawnManager.SPAWNS.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    String name = context.getArgument("name", String.class);

                                    if (!SpawnManager.SPAWNS.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Der Spawn " + name + " existiert nicht!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    if (SpawnManager.SPAWNS.size() == 1 && SpawnManager.SPAWNS.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Es ist nicht möglich, den einzigen verfügbaren Spawn zu löschen.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    SpawnManager.setSpawn(name, null);
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Der Spawn " + name + " wurde gelöscht!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
