package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.kit.Items;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.ReferencingInventory;
import xyz.xenondevs.invui.window.Window;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class InventoryCommands {
    public static final String ANVIL_PERMISSION = "lowdfx.anvil";
    public static final String ENDERSEE_PERMISSION = "lowdfx.endersee";
    public static final String INVSEE_PERMISSION = "lowdfx.invsee";
    public static final String TRASH_PERMISSION = "lowdfx.trash";
    public static final String WORKBENCH_PERMISSION = "lowdfx.workbench";

    public static LiteralCommandNode<CommandSourceStack> anvilCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("anvil")
                .requires(source -> source.getSender().hasPermission(ANVIL_PERMISSION) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                    player.openAnvil(player.getLocation(), true);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Der Amboss öffnet sich!", NamedTextColor.GREEN)));
                    return 1;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> enderseeCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("endersee")
                .requires(source -> source.getSender().hasPermission(ENDERSEE_PERMISSION) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                    player.openInventory(player.getEnderChest());
                    player.sendMessage(LowdFX.serverMessage(Component.text("Deine Enderchest wurde geöffnet!", NamedTextColor.GREEN)));
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                            player.openInventory(target.getEnderChest());
                            player.sendMessage(Component.text("Du hast die Enderchest von " + target.getName() + " geöffnet!", NamedTextColor.GREEN));
                            return 1;
                        })
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> invseeCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("invsee")
                .requires(source -> source.getSender().hasPermission(INVSEE_PERMISSION) && source.getExecutor() instanceof Player)
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                            PlayerInventory inv = target.getInventory();

                            Gui gui = Gui.normal().setStructure(
                                    ". h c l b . . o .", // h = Helmet
                                    ". . . . . . . . .", // c = Chestplate
                                    "I I I I I I I I I", // l = Leggings
                                    "I I I I I I I I I", // b = Boots
                                    "I I I I I I I I I", // o = Offhand
                                    "I I I I I I I I I") // I = Inventory
                                    .setBackground(Items.BLACK_BACKGROUND)
                                    .addIngredient('h', new Items.LiveItem(target, PlayerInventory::setHelmet, () -> Objects.requireNonNullElse(inv.getHelmet(), Items.WHITE_BACKGROUND)))
                                    .addIngredient('c', new Items.LiveItem(target, PlayerInventory::setChestplate, () -> Objects.requireNonNullElse(inv.getChestplate(), Items.WHITE_BACKGROUND)))
                                    .addIngredient('l', new Items.LiveItem(target, PlayerInventory::setLeggings, () -> Objects.requireNonNullElse(inv.getLeggings(), Items.WHITE_BACKGROUND)))
                                    .addIngredient('b', new Items.LiveItem(target, PlayerInventory::setBoots, () -> Objects.requireNonNullElse(inv.getBoots(), Items.WHITE_BACKGROUND)))
                                    .addIngredient('o', new Items.LiveItem(target, PlayerInventory::setItemInOffHand, inv::getItemInOffHand))
                                    .addIngredient('I', ReferencingInventory.fromReversedPlayerStorageContents(inv))
                                    .build();

                            Window window = Window.single().setTitle("Inventar von " + target.getName()).setGui(gui).setViewer(player).build();
                            window.open();

                            Bukkit.getScheduler().runTaskTimer(LowdFX.PLUGIN, r -> {
                                if (window.isOpen()) {
                                    window.notifyAll(); // Damit Änderungen vom Inventar live sind.
                                } else {
                                    r.cancel();
                                }
                            }, 10, 10);

                            player.sendMessage(Component.text("Du hast das Inventar von " + target.getName() + " geöffnet!", NamedTextColor.GREEN));
                            return 1;
                        })
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> trashCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("trash")
                .requires(source -> source.getSender().hasPermission(TRASH_PERMISSION) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                    player.openInventory(Bukkit.createInventory(player, 36, Component.text("Mülleimer").decorate(TextDecoration.BOLD)));
                    player.sendMessage(LowdFX.serverMessage(Component.text("Der Mülleimer öffnet sich!", NamedTextColor.GREEN)));
                    return 1;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> workbenchCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("workbench")
                .requires(source -> source.getSender().hasPermission(WORKBENCH_PERMISSION) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                    player.openWorkbench(player.getLocation(), true);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Die Werkbank öffnet sich!", NamedTextColor.GREEN)));
                    return 1;
                })
                .build();
    }
}
