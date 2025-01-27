package at.lowdfx.lowdfx.commands.chest.lock;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChestLockListener implements Listener {
    // Hilfsfunktion zur Abfrage der verbundenen Kisten
    private @NotNull Set<Location> getConnectedChests(Block chestBlock) {
        return Utilities.connectedChests(chestBlock);
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return; // Wenn der Block null ist, gehe zurück, ohne etwas zu tun.

        // Überprüfen, ob der Block existiert und eine Kiste ist.
        if (block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX")) {
            Location chestLocation = block.getLocation();
            ChestData data = Lowdfx.PLUGIN.getChestData();

            // Überprüfen, ob die Kiste gesperrt ist.
            if (data.isChestLocked(chestLocation)) {
                // Überprüfen, ob der Spieler berechtigt ist, auf die Kiste zuzugreifen.
                if (!data.isOwner(player.getName(), chestLocation) && !data.isPlayerInWhitelist(chestLocation, player.getName()) && !player.hasPermission(ChestLockCommand.ADMIN_PERMISSION)) {
                    // Zugriff verweigern, da der Spieler kein Besitzer ist oder nicht auf der Whitelist steht.
                    event.setCancelled(true);
                    player.sendMessage(Lowdfx.serverMessage(Component.text("Diese Kiste ist gesperrt! Du hast keinen Zugriff.", NamedTextColor.RED)));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(@NotNull InventoryMoveItemEvent event) {
        // Prüfe, ob die Quelle (Hopper zieht Items) oder Ziel (Items werden abgelegt) eine Kiste oder Shulkerkiste ist
        if (event.getSource().getHolder() instanceof BlockState blockState) {
            if (blockState.getBlock().getType().name().endsWith("CHEST") || blockState.getBlock().getType().name().endsWith("SHULKER_BOX")) {

                // Hole die Location von der Quelle
                Location chestLocation = blockState.getLocation();

                ChestData data = Lowdfx.PLUGIN.getChestData();
                if (data.isChestLocked(chestLocation)) {
                    // Überprüfe, ob ein Spieler die Aktion ausführt und die erforderliche Permission hat
                    Player player = null;

                    // Versuche, einen Player-Interagierer zu ermitteln
                    if (event.getInitiator().getHolder() instanceof Player) {
                        player = (Player) event.getInitiator().getHolder();
                    }

                    // Abbrechen, falls die Kiste gesperrt ist und der Spieler keine Permission hat
                    if (player == null || !player.hasPermission(ChestLockCommand.ADMIN_PERMISSION)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Genauso bei Ziel (Destination)
        if (event.getDestination().getHolder() instanceof BlockState blockState) {
            if (blockState.getBlock().getType().name().endsWith("CHEST") || blockState.getBlock().getType().name().endsWith("SHULKER_BOX")) {

                // Hole die Location von der Ziel-Box
                Location chestLocation = blockState.getLocation();

                ChestData data = Lowdfx.PLUGIN.getChestData();
                if (data.isChestLocked(chestLocation)) {
                    // Überprüfe, ob ein Spieler die Aktion ausführt und die erforderliche Permission hat
                    Player player = null;

                    // Versuche, einen Player-Interagierer zu ermitteln
                    if (event.getInitiator().getHolder() instanceof Player) {
                        player = (Player) event.getInitiator().getHolder();
                    }

                    // Abbrechen, falls die Kiste gesperrt ist und der Spieler keine Permission hat
                    if (player == null || !player.hasPermission(ChestLockCommand.ADMIN_PERMISSION))
                        event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        // Prüfe, ob der Block eine Kiste oder Shulker-Kiste ist
        if (type == Material.CHEST || type.name().endsWith("SHULKER_BOX")) {
            // Hole die Position der Kiste
            Location chestLocation = block.getLocation();
            ChestData data = Lowdfx.PLUGIN.getChestData();

            // Überprüfen, ob die Kiste gesperrt ist
            if (data.isChestLocked(chestLocation)) {
                // Überprüfen, ob der Spieler auf der Whitelist oder Besitzer ist
                if (!data.isPlayerInWhitelist(chestLocation, player.getName()) && !data.isOwner(player.getName(), chestLocation) && !player.hasPermission(ChestLockCommand.ADMIN_PERMISSION)) {
                    // Kiste ist gesperrt und der Spieler ist weder Besitzer noch auf der Whitelist
                    event.setCancelled(true);
                    player.sendMessage(Lowdfx.serverMessage(Component.text("Diese Kiste ist gesperrt und du kannst sie nicht abbauen.", NamedTextColor.RED)));
                } else {
                    // Spieler ist Besitzer oder auf der Whitelist, Kiste kann gelöscht werden
                    data.removeDestroyedChest(chestLocation);
                    player.sendMessage(Lowdfx.serverMessage(Component.text("Diese Kiste wurde gelöscht.", NamedTextColor.GREEN)));
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Wenn die platzierte Kiste nicht vom Typ CHEST ist, brechen wir ab.
        if (block.getType() != Material.CHEST && !block.getType().name().endsWith("SHULKER_BOX")) {
            return;
        }

        ChestData data = Lowdfx.PLUGIN.getChestData();

        // Prüfen, ob die platzierte Kiste eine Nachbarkiste hat, die gesperrt ist
        Set<Location> connectedChests = getConnectedChests(block);
        Location chestLocation = block.getLocation();
        for (Location adjacentLocation : connectedChests) {
            // Wenn die Kiste gesperrt ist
            if (data.isChestLocked(adjacentLocation)) {
                data.lockAdjacentChests(chestLocation, player.getName());
                // Verhindern, dass ein unberechtigter Spieler eine Doppelkiste erstellt
                if (!data.isPlayerInWhitelist(adjacentLocation, player.getName()) &&
                        !player.hasPermission(ChestLockCommand.ADMIN_PERMISSION)) {
                    event.setCancelled(true);
                    player.sendMessage(Lowdfx.serverMessage(Component.text("Du kannst keine Doppelkiste mit einer gesperrten Kiste erstellen.", NamedTextColor.RED)));
                    return;
                }
            }
        }
    }


    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        try {
            // Überprüfen, ob ChestData geladen ist
            if (Lowdfx.PLUGIN.getChestData() == null) {
                return;
            }

            // Überprüfe, ob es sich um eine Explosion handelt, die durch TNT, Creeper, etc. ausgelöst wurde
            if (event.getEntity() instanceof TNTPrimed || event.getEntity() instanceof Creeper || event.getEntity() instanceof Explosive || event.getEntity() instanceof ExplosiveMinecart) {
                // Alle gesperrten Kisten aus der Explosionsliste entfernen
                event.blockList().removeAll(explosionBlocks(event.blockList()));
            }
        } catch (Exception e) {
            Lowdfx.LOG.error("Irgendwas funktioniert nicht.", e);
        }
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        try {
            // Überprüfen, ob ChestData geladen ist
            if (Lowdfx.PLUGIN.getChestData() == null) return;
            // Alle gesperrten Kisten aus der Explosionsliste entfernen
            event.blockList().removeAll(explosionBlocks(event.blockList()));
        } catch (Exception e) {
            Lowdfx.LOG.error("Irgendwas funktioniert nicht.", e);
        }
    }

    // Liste von Blöcken, die von der Explosion betroffen sind
    public static @NotNull List<Block> explosionBlocks(@NotNull List<Block> blocks) {
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : blocks) {
            // Wenn es sich um eine Kiste handelt
            if (block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX")) {
                Location chestLocation = block.getLocation();
                ChestData data = Lowdfx.PLUGIN.getChestData();

                // Wenn die Kiste gesperrt ist, verhindern wir die Zerstörung
                if (data.isChestLocked(chestLocation)) {
                    // Block zur Liste der zu entfernenden Blöcke hinzufügen
                    blocksToRemove.add(block);
                }
            }
        }
        return blocksToRemove;
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        pistonStuff(event, event.getBlocks());
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        pistonStuff(event, event.getBlocks());
    }

    public void pistonStuff(BlockPistonEvent event, List<Block> blocks) {
        try {
            if (Lowdfx.PLUGIN.getChestData() == null) return; // Frühes Abbrechen, falls `chestData` nicht verfügbar ist.

            // Überprüfe, ob einer der Blöcke gesperrt ist, bevor der Block gezogen/gepusht wird.
            for (Block block : blocks) {
                if (block.getType() == Material.CHEST || block.getType().name().endsWith("SHULKER_BOX")) {
                    // Wenn die Kiste gesperrt ist, verhindere das Ziehen/Pushen.
                    if (Lowdfx.PLUGIN.getChestData().isChestLocked(block.getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Lowdfx.LOG.error("Irgendwas funktioniert nicht.", e);
        }
    }
}