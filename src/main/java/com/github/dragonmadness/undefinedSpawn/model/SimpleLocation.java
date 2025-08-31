package com.github.dragonmadness.undefinedSpawn.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public record SimpleLocation(String world, Double x, Double y, Double z, Float yaw, Float pitch) {
    public static SimpleLocation of(Location bukkitLocation) {
        return new SimpleLocation(
                bukkitLocation.getWorld().getName(),
                bukkitLocation.getX(),
                bukkitLocation.getY(),
                bukkitLocation.getZ(),
                bukkitLocation.getYaw(),
                bukkitLocation.getPitch()
        );
    }

    public Location asBukkitLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }
}
