package com.github.dragonmadness.undefinedSpawn;

import com.github.dragonmadness.undefinedSpawn.model.SimpleLocation;
import com.github.dragonmadness.undefinedSpawn.model.Teleportation;
import com.github.dragonmadness.undefinedSpawn.storage.JsonStorage;
import com.github.dragonmadness.undefinedSpawn.storage.Storage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class StorageTest {
    private final static Path TEST_FILE = Path.of("C:\\WORK\\Java\\Projects\\UndefinedSpawn\\src\\test\\resources\\file.json");

    @Test
    public void testGetParentNode() {
        Assertions.assertEquals("messages", JsonStorage.getNodeParentLocation("messages.main-command"));
    }

    @Test
    public void testStorageColdLoad() {
        Assertions.assertDoesNotThrow(() -> {
            Files.deleteIfExists(TEST_FILE);
            new JsonStorage(TEST_FILE).load();
        });
    }

    @Test
    public void testStorageWriteAndRead() {
        Assertions.assertDoesNotThrow(() -> {
            Storage storage = new JsonStorage(TEST_FILE).load();
            String key = "main";
            String value = "test string";

            storage.write(key, value);
            Assertions.assertEquals(value, storage.read(key, String.class));
        });
    }

    @Test
    public void testWriteAndReadTeleportation() {
        Assertions.assertDoesNotThrow(() -> {
            Storage storage = new JsonStorage(TEST_FILE).load();
            String key = "simpleTele";
            Teleportation value = new Teleportation("testPlayer", 9999999L,
                    new SimpleLocation("world", 2D, 5D, 0D, 90F, 0F));

            storage.write(key, value);
            Assertions.assertEquals(value, storage.read(key, Teleportation.class));
        });
    }

}
