package com.github.dragonmadness.undefinedSpawn.manager;

import com.github.dragonmadness.undefinedSpawn.model.SimpleLocation;
import com.github.dragonmadness.undefinedSpawn.model.Teleportation;
import com.github.dragonmadness.undefinedSpawn.storage.Storage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class TeleportationManager {
    private final static Logger log = Bukkit.getLogger();

    private static final String TELEPORTATIONS_PATH = "teleportations";
    private static final String SPAWN_PATH = "spawn";

    private final Plugin plugin;
    private final Storage dataStorage;

    private final List<Teleportation> teleportations = new ArrayList<>();
    @Getter
    private Location spawnLocation = null;
    private BukkitTask writingTask = null;

    public TeleportationManager load() throws IOException {
        teleportations.clear();
        try {
            teleportations.addAll(dataStorage.readList(TELEPORTATIONS_PATH, Teleportation.class));
        } catch (IOException e) {
            log.warning("Teleportation transactions not loaded because of: %s".formatted(e.getMessage()));
            log.warning("The field will be overwritten to prevent future errors.");
            dataStorage.write(TELEPORTATIONS_PATH, teleportations);
        }

        try {
            spawnLocation = dataStorage.read("spawn", SimpleLocation.class).asBukkitLocation();
        } catch (IOException e) {
            log.warning("Spawnpoint not loaded because of: %s".formatted(e.getMessage()));
            log.warning("The field will be overwritten to prevent future errors.");
            spawnLocation = Bukkit.getWorld("world").getSpawnLocation();
            dataStorage.write(SPAWN_PATH, SimpleLocation.of(spawnLocation));
        }

        return this;
    }

    public void destroy() {
        if (writingTask != null) writingTask.cancel();

        wrappedWrite(TELEPORTATIONS_PATH, teleportations);
        wrappedWrite(SPAWN_PATH, SimpleLocation.of(spawnLocation));
    }

    public void wrappedWrite(String path, Object value) {
        try {
            dataStorage.write(path, value);
        } catch (IOException e) {
            log.severe("Exception when writing to persistent storage! Data will NOT be written.");
            e.printStackTrace();
        }
    }

    /**
     * Creates an asyncronous Bukkit task to write data.
     * <p>
     * If a task is already in progress, drops the requested update. Use syncronous write() to avoid data loss!
     */
    public void writeAsync(String path, Object value) {
        if (writingTask == null) {
            writingTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                wrappedWrite(path, value);
                writingTask = null;
            });
        }
    }

    public Teleportation getLastTeleportation(String playerName) {
        return teleportations.stream().filter(n -> n.getPlayer().equals(playerName))
                .findFirst().orElse(null);
    }

    public void teleportSpawn(Player player) {
        Teleportation lastTeleportation = getLastTeleportation(player.getName());
        if (lastTeleportation != null) teleportations.remove(lastTeleportation);
        teleportations.add(new Teleportation(
                player.getName(),
                System.currentTimeMillis(),
                SimpleLocation.of(player.getLocation())
        ));
        safeTeleport(player, spawnLocation);
        log.info("Teleported player %s to spawn successfully".formatted(player.getName()));
        writeAsync(TELEPORTATIONS_PATH, teleportations);
    }

    public void teleportBack(Player player) {
        Teleportation lastTeleportation = getLastTeleportation(player.getName());
        safeTeleport(player, lastTeleportation.getOrigin().asBukkitLocation());
        log.info("Teleported player %s back to their original spot successfully".formatted(player.getName()));
        lastTeleportation.setReturned(true);
        writeAsync(TELEPORTATIONS_PATH, teleportations);
    }

    public void updateSpawn(Location newSpawn) {
        spawnLocation = newSpawn;
        writeAsync(SPAWN_PATH, SimpleLocation.of(spawnLocation));
    }

    private void safeTeleport(Player player, Location target) {
        player.setFallDistance(0);
        player.teleport(target);
    }
}
