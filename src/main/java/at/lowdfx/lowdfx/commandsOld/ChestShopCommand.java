package at.lowdfx.lowdfx.commandsOld;

import at.lowdfx.lowdfx.inventory.ShopData;
import at.lowdfx.lowdfx.managers.ChestShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChestShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /shop create <price> or /shop add <player>", NamedTextColor.RED));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create" -> {
                if (args.length != 2) {
                    player.sendMessage(Component.text("Usage: /shop create <price>", NamedTextColor.RED));
                    return true;
                }

                int price;
                try {
                    price = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Component.text("Price must be a valid number.", NamedTextColor.RED));
                    return true;
                }

                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null || !(targetBlock.getType() == Material.CHEST || targetBlock.getType() == Material.SHULKER_BOX)) {
                    player.sendMessage(Component.text("You must be looking at a chest or shulker box to create a shop.", NamedTextColor.RED));
                    return true;
                }

                Location location = targetBlock.getLocation();

                if (ChestShopManager.isShop(location)) {
                    player.sendMessage(Component.text("This chest is already a shop.", NamedTextColor.RED));
                    return true;
                }

                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.isEmpty()) {
                    player.sendMessage(Component.text("You must hold an item to set as the shop's product.", NamedTextColor.RED));
                    return true;
                }

                ShopData shopData = new ShopData(player.getUniqueId(), location, heldItem.clone(), price);
                ChestShopManager.registerShop(player.getUniqueId(), location, shopData);
                player.sendMessage(Component.text("Shop created successfully!", NamedTextColor.GREEN));
            }
            case "add" -> {
                if (args.length != 2) {
                    player.sendMessage(Component.text("Usage: /shop add <player>", NamedTextColor.RED));
                    return true;
                }

                String targetPlayerName = args[1];
                Player targetPlayer = player.getServer().getPlayerExact(targetPlayerName);

                if (targetPlayer == null) {
                    player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                    return true;
                }

                Block targetBlockForWhitelist = player.getTargetBlockExact(5);
                if (targetBlockForWhitelist == null || !ChestShopManager.isShop(targetBlockForWhitelist.getLocation())) {
                    player.sendMessage(Component.text("You must be looking at your shop to whitelist a player.", NamedTextColor.RED));
                    return true;
                }

                Location shopLocation = targetBlockForWhitelist.getLocation();
                if (!ChestShopManager.isOwner(player.getUniqueId(), shopLocation)) {
                    player.sendMessage(Component.text("You are not the owner of this shop.", NamedTextColor.RED));
                    return true;
                }

                ChestShopManager.whitelistPlayer(shopLocation, targetPlayer.getUniqueId());
                player.sendMessage(Component.text("Player " + targetPlayerName + " has been whitelisted for this shop.", NamedTextColor.GREEN));
            }
            default -> player.sendMessage(Component.text("Unknown subcommand. Use: /shop create <price> or /shop add <player>.", NamedTextColor.RED));
        }
        return true;
    }
}
