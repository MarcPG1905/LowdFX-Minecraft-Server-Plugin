package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.managers.teleport.TeleportManager;
import com.marcpg.libpg.storing.Cord;
import com.marcpg.libpg.storing.CordMinecraftAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SimpleLocation(String world, Cord cord) {
    public void teleportSafe(Entity entity) {
        TeleportManager.teleportSafe(entity, asLocation());
    }

    public @NotNull Location asLocation() {
        return CordMinecraftAdapter.toLocation(cord, Objects.requireNonNull(Bukkit.createWorld(WorldCreator.name(world))));
    }

    public static @NotNull SimpleLocation ofLocation(@NotNull Location location) {
        return new SimpleLocation(location.getWorld().getName(), CordMinecraftAdapter.ofLocation(location));
    }
}
