package com.github.dragonmadness.undefinedSpawn;

import com.github.dragonmadness.undefinedSpawn.command.AdminCommand;
import com.github.dragonmadness.undefinedSpawn.command.PlayerCommand;
import com.github.dragonmadness.undefinedSpawn.listener.PlayerListener;
import com.github.dragonmadness.undefinedSpawn.manager.TeleportationManager;
import com.github.dragonmadness.undefinedSpawn.storage.JsonStorage;
import com.github.dragonmadness.undefinedSpawn.storage.Storage;
import com.github.dragonmadness.undefinedSpawn.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class UndefinedSpawn extends JavaPlugin {
    private final static Logger log = Bukkit.getLogger();

    // CONFIG
    private Config config;

    // STORAGE
    private Storage dataStorage;

    // MANAGERS
    private TeleportationManager teleportationManager;

    // COMMANDS
    private AdminCommand adminCommand;
    private PlayerCommand playerCommand;

    // LISTENERS
    private PlayerListener playerListener;

    @Override
    public void onEnable() {
        loadConfig();
        loadStorage();
        loadManagers();
        loadCommands();
        loadListeners();
    }

    @Override
    public void onDisable() {
        if (dataStorage == null || !dataStorage.isLoaded()) {
            return;
        }
        if (teleportationManager != null) {
            teleportationManager.destroy();
        }
    }

    private void loadConfig() {
        try {
            config = new Config(
                    this,
                    getDataFolder().toPath().resolve("config.yml"),
                    Path.of("config.yml")
            ).load();
        } catch (IOException e) {
            log.severe("Plugin failed to load config! Stopping.");
            e.printStackTrace();
            this.setEnabled(false);
        }
    }

    private void loadStorage() {
        try {
            dataStorage = new JsonStorage(this.getDataFolder().toPath().resolve("data.json")
            ).load();
        } catch (IOException e) {
            log.severe("Plugin failed to load storage! Stopping.");
            e.printStackTrace();
            this.setEnabled(false);
        }
    }

    private void loadManagers() {
        try {
            teleportationManager = new TeleportationManager(this, dataStorage).load();
        } catch (IOException e) {
            log.severe("Unable to read from storage. Stopping");
            e.printStackTrace();
            this.setEnabled(false);
        }
    }

    private void loadCommands() {
        adminCommand = new AdminCommand(config, teleportationManager);
        getCommand("setspawn").setExecutor(adminCommand);
        getCommand("undefinedspawn").setExecutor(adminCommand);
        playerCommand = new PlayerCommand(config, teleportationManager, this);
        getCommand("spawn").setExecutor(playerCommand);
        getCommand("back").setExecutor(playerCommand);
    }

    private void loadListeners() {
        playerListener = new PlayerListener(playerCommand);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
    }
}
