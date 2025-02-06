package at.lowdfx.lowdfx.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.FileUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VanishingHandler {
    private static final BossBar BOSS_BAR = BossBar.bossBar(Component.text("Vanish"), 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    private static final Set<UUID> VANISHED = new HashSet<>();

    // Spieler unsichtbar machen und BossBar anzeigen.
    public static void makePlayerInvisible(@NotNull Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.hidePlayer(LowdFX.PLUGIN, player);
            }
        }

        player.setMetadata("vanished", new FixedMetadataValue(LowdFX.PLUGIN, true));
        BOSS_BAR.addViewer(player);
        VANISHED.add(player.getUniqueId());
    }

    // Spieler sichtbar machen und BossBar wegmachen.
    public static void makePlayerVisible(@NotNull Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.showPlayer(LowdFX.PLUGIN, player);
            }
        }

        player.removeMetadata("vanished", LowdFX.PLUGIN);
        BOSS_BAR.removeViewer(player);
        VANISHED.remove(player.getUniqueId());
    }

    public static void saveAll() {
        FileUtils.save(VANISHED, "vanished.json");
    }

    public static void loadAll() {
        VANISHED.addAll(FileUtils.load("vanished.json", List.of()));
    }

    public static Set<UUID> getVanishedPlayers() {
        return VANISHED;
    }
}
