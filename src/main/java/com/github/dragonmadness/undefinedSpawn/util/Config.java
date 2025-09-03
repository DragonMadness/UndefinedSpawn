package com.github.dragonmadness.undefinedSpawn.util;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.github.dragonmadness.undefinedSpawn.storage.Storage;
import com.github.dragonmadness.undefinedSpawn.storage.YamlStorage;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class Config {
    private final static Logger log = Bukkit.getLogger();

    private final Plugin plugin;
    private final Path configPath;
    private final Path templateResourcePath;
    private Storage fileStorage;

    public Config load() throws IOException {
        fileStorage = new YamlStorage(configPath).load();
        fileStorage.addMissingFields(readPackagedResource());
        return this;
    }

    private InputStream readPackagedResource() {
        return plugin.getResource(templateResourcePath.toString());
    }

    public <T> T readOrDefault(Class<T> type, String path, T defaultValue) {
        try {
            if (fileStorage.getFieldType(path) == JsonNodeType.ARRAY) {
                List<T> variants = fileStorage.readList(path, type);
                return variants.get(ThreadLocalRandom.current().nextInt(variants.size()));
            } else {
                return fileStorage.read(path, type);
            }
        } catch (IOException e) {
            log.warning("Unable to read field %s from file %s".formatted(path, this.configPath));
            Arrays.stream(e.getStackTrace()).forEach(n -> log.warning(n.toString()));
            return defaultValue;
        }
    }

    public <T> T readOrDefault(String path, T defaultValue) {
        return readOrDefault((Class<T>) defaultValue.getClass(), path, defaultValue);
    }

    public String getString(String path) {
        return readOrDefault(String.class, path, null);
    }

    public Integer getInt(String path) {
        return readOrDefault(Integer.class, path, null);
    }

    public Double getDouble(String path) {
        return readOrDefault(Double.class, path, null);
    }

    public Boolean getBoolean(String path) {
        return readOrDefault(Boolean.class, path, false);
    }

    public boolean isLoggingEnabled(String category) {
        return readOrDefault("logging.%s".formatted(category), false);
    }
}

