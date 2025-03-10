package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.event.ConnectionEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public final class Utilities {
    public static long currentTimeSecs() {
        return (long) (System.currentTimeMillis() * 0.001);
    }

    public static <K, V> Map.@Nullable Entry<K, V> getEntryByValue(@NotNull Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value))
                return entry;
        }
        return null;
    }

    public static String getServerProperty(@NotNull String key) {
        try (InputStream input = new FileInputStream(new File(Bukkit.getWorldContainer(), "server.properties"))) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty(key);
        } catch (IOException e) {
            LowdFX.LOG.error("Konnte nicht server.properties lesen.", e);
            throw new RuntimeException(e);
        }
    }

    public static Block connectedChest(Block block) {
        if (block != null && block.getBlockData() instanceof Chest chestBlockData && chestBlockData.getType() != Chest.Type.SINGLE) {
            BlockFace direction = switch (chestBlockData.getFacing()) {
                case NORTH -> chestBlockData.getType() == Chest.Type.LEFT ? BlockFace.EAST : BlockFace.WEST;
                case EAST -> chestBlockData.getType() == Chest.Type.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
                case SOUTH -> chestBlockData.getType() == Chest.Type.LEFT ? BlockFace.WEST : BlockFace.EAST;
                case WEST -> chestBlockData.getType() == Chest.Type.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
                default -> null;
            };
            return direction == null ? null : block.getRelative(direction);
        }
        return null;
    }

    public static @NotNull Component joinMessage(@NotNull Player player) {
        return LowdFX.serverMessage((player.hasPlayedBefore() ? ConnectionEvents.JOIN_MESSAGE : ConnectionEvents.FIRST_JOIN_MESSAGE)
                .replaceText(TextReplacementConfig.builder().match("{0}").replacement(player.name()).build()));
    }

    public static @NotNull Component quitMessage(@NotNull Player player) {
        return LowdFX.serverMessage(ConnectionEvents.QUIT_MESSAGE.replaceText(TextReplacementConfig.builder().match("{0}").replacement(player.name()).build()));
    }
}
