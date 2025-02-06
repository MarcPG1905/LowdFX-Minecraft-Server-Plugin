package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.HomeManager;
import at.lowdfx.lowdfx.managers.PlaytimeManager;
import at.lowdfx.lowdfx.managers.SpawnManager;
import at.lowdfx.lowdfx.util.PlaytimeInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ConnectionEvents implements Listener {
    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Quit Message
        if (player.hasPlayedBefore()) {
            event.joinMessage(LowdFX.serverMessage(Component.text(Objects.requireNonNullElse(LowdFX.CONFIG.getString("join.welcome"), "???"), NamedTextColor.YELLOW)
                    .appendSpace()
                    .append(player.name().color(NamedTextColor.GOLD))
                    .append(Component.text("!", NamedTextColor.YELLOW))));
        } else {
            event.joinMessage(LowdFX.serverMessage(Component.text("Heißt ", NamedTextColor.YELLOW)
                    .append(player.name().color(NamedTextColor.GOLD))
                    .append(Component.text(" willkommen, er ist das erste Mal gejoint", NamedTextColor.YELLOW))));
        }

        // Playtime
        if (PlaytimeManager.PLAYTIMES.containsKey(uuid)) {
            PlaytimeManager.PLAYTIMES.get(uuid).login();
        } else {
            PlaytimeManager.PLAYTIMES.put(uuid, new PlaytimeInfo(uuid));
        }

        // Homes
        HomeManager.load(player);
        HomeManager.load(player.getUniqueId());

        // Spawns
        SpawnManager.getSpawn(event.getPlayer()).teleport(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Quit Message
        event.quitMessage(LowdFX.serverMessage(Component.text(Objects.requireNonNullElse(LowdFX.CONFIG.getString("join.quit"), "???"), NamedTextColor.YELLOW))
                .appendSpace()
                .append(player.name().color(NamedTextColor.GOLD))
                .append(Component.text("!", NamedTextColor.YELLOW)));

        // Playtime
        if (PlaytimeManager.PLAYTIMES.containsKey(uuid)) {
            PlaytimeManager.PLAYTIMES.get(uuid).logout();
        }

        // Homes
        HomeManager.save(player.getUniqueId());

        // Spawns
        player.setRespawnLocation(SpawnManager.getSpawn(player).location(), true);
        Bukkit.getScheduler().runTaskAsynchronously(LowdFX.PLUGIN, () -> {
            if (!event.getPlayer().hasPlayedBefore()) {
                SpawnManager.getSpawn(event.getPlayer()).teleport(event.getPlayer());
            }
        });
    }
}
