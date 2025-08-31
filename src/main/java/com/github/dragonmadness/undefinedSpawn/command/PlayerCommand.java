package com.github.dragonmadness.undefinedSpawn.command;

import com.github.dragonmadness.undefinedSpawn.command.util.CommandUtil;
import com.github.dragonmadness.undefinedSpawn.manager.TeleportationManager;
import com.github.dragonmadness.undefinedSpawn.model.Teleportation;
import com.github.dragonmadness.undefinedSpawn.util.Config;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PlayerCommand extends ConfiguredCommand {
    private final static Logger log = Bukkit.getLogger();
    private final TeleportationManager teleportationManager;
    private final Plugin plugin;

    @Getter
    private final Map<Player, BukkitTask> teleportationTasks = new HashMap<>();

    public PlayerCommand(Config config, TeleportationManager teleportationManager, Plugin plugin) {
        super(config);
        this.teleportationManager = teleportationManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player player)) {
            respond(sender, "messages.common.not-player");
            return true;
        }

        if (teleportationTasks.containsKey(player)) {
            respond(player, "messages.common.teleportation-in-progress");
            return true;
        }

        switch (command.getName()) {
            case "spawn": { spawnCommand(player); break; }
            case "back": { backCommand(player); break; }
            default: respond(sender, "messages.common.unknown-command");
        }
        return true;
    }

    private void spawnCommand(Player target) {
        if (calculateHungerCost(target.getLocation(), teleportationManager.getSpawnLocation()) >= 1
                && target.getFoodLevel() + target.getSaturation() < 2.0) {
            respond(
                    target,
                    "messages.spawn.not-enough-food"
            );
        }

        Teleportation teleportation = teleportationManager.getLastTeleportation(target.getName());
        int coolDown = config.getInt("main.teleport.cooldown") * 1000; // in millis
        if (teleportation == null || System.currentTimeMillis() - teleportation.getTime() >= coolDown) {
            scheduleTeleportation(target, true);
        } else {
            respond(
                    target,
                    "messages.common.on-cool-down",
                    String.valueOf((coolDown - (System.currentTimeMillis() - teleportation.getTime())) / 1000)
            );
        }
    }

    private void backCommand(Player target) {
        Teleportation teleportation = teleportationManager.getLastTeleportation(target.getName());
        if (teleportation != null && !teleportation.isReturned()) {
            scheduleTeleportation(target, false);
        } else if (teleportation == null) {
            respond(target, "messages.back.no-origin");
        } else {
            respond(target, "messages.back.already-returned");
        }
    }

    private void scheduleTeleportation(Player player, boolean isSpawn) {
        Integer timeout = config.getInt("main.teleport.timeout");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
            teleportationTasks.put(player, task);

            for (int i = 0; i < timeout; i++) {
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacy(CommandUtil.translateColorCodes(
                                config.getString("messages.common.teleportation-countdown")
                                        .formatted(timeout - i)))
                );
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    task.cancel();
                    return;
                }
            }

            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy(CommandUtil.translateColorCodes(
                            config.getString(isSpawn ? "messages.spawn.success" : "messages.back.success")))
            );

            int hungerCost = 0;
            if (isSpawn) {
                if (config.readOrDefault("main.teleport.hunger-cost.enabled.spawn", false)) {
                    hungerCost = calculateHungerCost(player.getLocation(), teleportationManager.getSpawnLocation());
                }
                Bukkit.getScheduler().runTask(plugin, () -> teleportationManager.teleportSpawn(player));
            } else {
                if (config.readOrDefault("main.teleport.hunger-cost.enabled.back", false)) {
                    hungerCost = calculateHungerCost(player.getLocation(),
                            teleportationManager.getLastTeleportation(player.getName()).getOrigin().asBukkitLocation());
                }
                Bukkit.getScheduler().runTask(plugin, () -> teleportationManager.teleportBack(player));
            }

            if (player.getGameMode() == GameMode.SURVIVAL) {
                int saturation = (int) player.getSaturation();
                player.setSaturation(Math.max(saturation - hungerCost, 0));
                hungerCost = Math.max(hungerCost - saturation, 0);
                int foodLevel = player.getFoodLevel();
                player.setFoodLevel(Math.max(foodLevel - hungerCost, 2));
            }

            teleportationTasks.remove(player);
        });
    }

    private int calculateHungerCost(Location origin, Location target) {
        double distance = Math.floor(origin.distance(target));
        String relation = config.getString("main.teleport.hunger-cost.relation");
        double multiplier = config.getDouble("main.teleport.hunger-cost.multiplier");

        double calc = switch (relation) {
            case "LINEAR" -> distance * multiplier;
            case "SQRT" -> Math.sqrt(distance) * multiplier;
            case "SQUARE" -> Math.pow(distance, 2) * multiplier;
            default -> -1;
        };
        if (calc == -1) {
            log.severe("No relation for hunger cost named %s. Calculation exited with a result of 0."
                    .formatted(relation));
            return 0;
        }

//        log.warning("distance %s cost %s".formatted(String.valueOf(distance), String.valueOf((int) Math.floor(calc))));

        return (int) Math.floor(calc);
    }
}
