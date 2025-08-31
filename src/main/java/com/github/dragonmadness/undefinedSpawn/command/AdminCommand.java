package com.github.dragonmadness.undefinedSpawn.command;

import com.github.dragonmadness.undefinedSpawn.command.util.CommandUtil;
import com.github.dragonmadness.undefinedSpawn.manager.TeleportationManager;
import com.github.dragonmadness.undefinedSpawn.util.Config;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class AdminCommand extends ConfiguredCommand {
    private final TeleportationManager teleportationManager;

    public AdminCommand(Config config, TeleportationManager teleportationManager) {
        super(config);
        this.teleportationManager = teleportationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        switch (command.getName()) {
            case "setspawn": {
                setSpawnCommand(sender);
                break;
            }
            case "undefinedspawn": {
                undefinedSpawnCommand(sender, strings);
                break;
            }
            default:
                respond(sender, "messages.common.unknown-command");
        }
        return true;
    }

    private void setSpawnCommand(CommandSender sender) {
        if (!(sender instanceof Player target)) {
            respond(sender, "messages.common.not-player");
            return;
        }
        if (!target.hasPermission("undefined-spawn.command.setspawn")) {
            respond(target, "messages.common.insufficient-perms");
            return;
        }

        Location newSpawnPoint = CommandUtil.centerLocation(target.getLocation());
        teleportationManager.updateSpawn(newSpawnPoint);
        respond(target, "messages.set-spawn.success");
        respond(target, "messages.set-spawn.new-spawn-point",
                String.valueOf(newSpawnPoint.getX()),
                String.valueOf(newSpawnPoint.getY()),
                String.valueOf(newSpawnPoint.getZ()),
                String.valueOf(newSpawnPoint.getYaw()),
                String.valueOf(newSpawnPoint.getPitch())
        );
    }

    private void undefinedSpawnCommand(CommandSender target, String... args) {
        if (!target.hasPermission("undefined-spawn.command.undefinedspawn")) {
            respond(target, "messages.common.insufficient-perms");
            return;
        }
        if (args.length == 0) {
            respond(target, "messages.common.insufficient-args");
            respond(target, "messages.main-command.help");
            return;
        }

        switch (args[0]) {
            case "reload": {
                try {
                    config.load();
                    respond(target, "messages.main-command.reload.success");
                } catch (IOException e) {
                    respond(target, "messages.main-command.reload.fail");
                }
                break;
            }
            default: {
                respond(target, "messages.common.unknown-command");
                respond(target, "messages.main-command.help");
                break;
            }
        }
    }
}
