package com.github.dragonmadness.undefinedSpawn.listener;

import com.github.dragonmadness.undefinedSpawn.command.PlayerCommand;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

@RequiredArgsConstructor
public class PlayerListener implements Listener {
    private final PlayerCommand playerCommand;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quitting = event.getPlayer();
        Map<Player, BukkitTask> teleportationTasks = playerCommand.getTeleportationTasks();
        if (teleportationTasks.containsKey(quitting)) {
            teleportationTasks.remove(quitting).cancel();
        }
    }
}
