package com.github.dragonmadness.undefinedSpawn.command;

import com.github.dragonmadness.undefinedSpawn.command.util.CommandUtil;
import com.github.dragonmadness.undefinedSpawn.util.Config;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public abstract class ConfiguredCommand implements CommandExecutor {
    protected final Config config;

    protected void respondRaw(CommandSender sender, String message, String... args) {
        String prefix = config.getString("messages.prefix");
        sender.sendMessage(CommandUtil.translateColorCodes("%s %s"
                .formatted(prefix, message.formatted((Object[]) args))));
    }

    protected void respond(CommandSender sender, String messageConfigKey, String... args) {
        respondRaw(sender, config.getString(messageConfigKey), args);
    }
}
