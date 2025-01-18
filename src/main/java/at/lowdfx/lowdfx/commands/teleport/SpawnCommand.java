package at.lowdfx.lowdfx.commands.teleport;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.commands.teleport.managers.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpawnCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.spawn.setremove";
    public static final String PLAYER_PERMISSION = "lowdfx.spawn";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(Lowdfx.PLUGIN, () -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Das darf nur ein Spieler tun!");
                return;
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 0) {
                    spawn(sender);
                    return;
                }

                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("help")) {
                        sendHelp(sender);
                        return;
                    }
                }
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("tp")) {
                        spawnAdmin(sender, args[1]);
                        return;
                    }
                }
            }

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("set")) {
                        setAdmin(sender, args[1], args);
                        return;
                    }

                    if (args[0].equalsIgnoreCase("remove")) {
                        removeAdmin(sender, args[1]);
                        return;
                    }
                }
            }

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +
                    ChatColor.RED + "Fehler! Benutze /spawn help für eine hilfe!");
        });
        return true;
    }

    private void removeAdmin(CommandSender sender, String name) {
        // Überprüfen, ob der Spawn existiert und sicherstellen, dass mehr als ein Spawn vorhanden ist
        if (SpawnManager.exists(name)) {
            // Prüfen, ob nur der aktuelle Spawn der einzige Spawn ist
            if (SpawnManager.getNames().size() == 1 && SpawnManager.getNames().contains(name)) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +
                        ChatColor.RED + "Es ist nicht möglich, den einzigen verfügbaren Spawn zu löschen.");
                return;
            }

            // Entferne den Spawn, falls vorhanden
            SpawnManager.setSpawn(name, null);
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +
                    ChatColor.GREEN + "Der Spawn " + ChatColor.BOLD + name +  ChatColor.GREEN + " wurde gelöscht!");
        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +
                    ChatColor.RED + "Der Spawn " + ChatColor.BOLD + name +  ChatColor.RED + " existiert nicht!");
        }
    }

    private void setAdmin(CommandSender sender, String name, String[] args) {
        // Setzen des Spawns
        SpawnManager.setSpawn(name, ((Player) sender).getLocation());
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +
                ChatColor.GREEN + "Der Spawn " + ChatColor.BOLD + name +  ChatColor.GREEN + " wurde gesetzt!");

        // Wenn nur ein Spawn existiert, den Standardspawn wiederherstellen
        if (SpawnManager.getNames().size() == 1) {
            // Neue Version ohne ServerProperties
            SpawnManager.setSpawn("default", Bukkit.getWorlds().get(0).getSpawnLocation());  // Hier wird die erste Welt im Server genutzt

        }
    }

    private void spawnAdmin(CommandSender sender, String name) {
        if (SpawnManager.exists(name)) {
            Objects.requireNonNull(SpawnManager.getSpawn(name)).teleport(((Player) sender), Lowdfx.PLUGIN);
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +
                    ChatColor.GREEN + "Du wurdest zum Spawn teleportiert!");
        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +
                    ChatColor.RED + "Der eingegebene Spawn " + ChatColor.BOLD + name +  ChatColor.RED + " existiert nicht!");
        }
    }

    private void spawn(CommandSender sender) {
        SpawnManager.getSpawn(((Player) sender)).teleport(((Player) sender), Lowdfx.PLUGIN);
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +
                ChatColor.GREEN + "Du wurdest zum Spawn teleportiert!");
    }

    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE + " → ";

        sender.sendMessage(title + ChatColor.BOLD + "------- Help: Spawn -------");
        sender.sendMessage(commandColor + "/spawn " +  arrow + color +" Teleportiert dich zum Spawn.");
        sender.sendMessage(commandColor + "/spawn help " +  arrow + color +" Sendet dir eine Hilfestellung.");

        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(commandColor + "/spawn set <name> " +  arrow + color +" Setzt einen Spawn mit dem Namen.");
            sender.sendMessage(commandColor + "/spawn remove <name> " +  arrow + color +" Löscht einen Spawn mit den Namen.");
            sender.sendMessage(commandColor + "/spawn tp <name> " +  arrow + color +" Teleportiert dich zum Spawn mit den eingegebenen Namen.");
        }
    }
}
