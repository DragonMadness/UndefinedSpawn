package com.github.dragonmadness.undefinedSpawn.command.util;

import org.bukkit.Location;

import java.util.regex.Pattern;

public class CommandUtil {

    public static String translateColorCodes(String origin) {
        return Pattern.compile("(&)(?=[0-9a-r])").matcher(origin).replaceAll("§");
    }

    public static Location centerLocation(Location location) {
        return new Location(
                location.getWorld(),
                Math.floor(location.getX()) + 0.5,
                Math.floor(location.getY()),
                Math.floor(location.getZ()) + 0.5,
                Math.round(location.getYaw() / 90D) * 90F,
                0F);
    }

}
