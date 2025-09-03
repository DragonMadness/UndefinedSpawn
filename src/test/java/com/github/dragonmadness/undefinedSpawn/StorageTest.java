package com.github.dragonmadness.undefinedSpawn;

import com.github.dragonmadness.undefinedSpawn.model.SimpleLocation;
import com.github.dragonmadness.undefinedSpawn.model.Teleportation;
import com.github.dragonmadness.undefinedSpawn.storage.JsonStorage;
import com.github.dragonmadness.undefinedSpawn.storage.Storage;
import com.github.dragonmadness.undefinedSpawn.storage.YamlStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class StorageTest {
    private final static Path TEST_JSON =
            Path.of("C:\\WORK\\Java\\Projects\\UndefinedSpawn\\src\\test\\resources\\file.json");
    private final static Path TEST_YAML =
            Path.of("C:\\WORK\\Java\\Projects\\UndefinedSpawn\\src\\test\\resources\\test.yml");
    private final static Path TEST_YAML_TEMPLATE =
            Path.of("C:\\WORK\\Java\\Projects\\UndefinedSpawn\\src\\test\\resources\\yamlTemplate.yml");

    @Test
    public void testGetParentNode() {
        Assertions.assertEquals("messages", JsonStorage.getNodeParentLocation("messages.main-command"));
    }

    @Test
    public void testStorageColdLoad() {
        Assertions.assertDoesNotThrow(() -> {
            Files.deleteIfExists(TEST_JSON);
            new JsonStorage(TEST_JSON).load();
        });
    }

    @Test
    public void testStorageWriteAndRead() {
        Assertions.assertDoesNotThrow(() -> {
            Storage storage = new JsonStorage(TEST_JSON).load();
            String key = "main";
            String value = "test string";

            storage.write(key, value);
            Assertions.assertEquals(value, storage.read(key, String.class));
        });
    }

    @Test
    public void testWriteAndReadTeleportation() {
        Assertions.assertDoesNotThrow(() -> {
            Storage storage = new JsonStorage(TEST_JSON).load();
            String key = "simpleTele";
            Teleportation value = new Teleportation("testPlayer", 9999999L,
                    new SimpleLocation("world", 2D, 5D, 0D, 90F, 0F));

            storage.write(key, value);
            Assertions.assertEquals(value, storage.read(key, Teleportation.class));
        });
    }

    @Test
    public void testAddMissingFields() {
        Assertions.assertDoesNotThrow(() -> {
            Files.deleteIfExists(TEST_YAML);
            Storage storage = new YamlStorage(TEST_YAML).load();

            storage.write("key1.text", "test text");

            storage.addMissingFields(Files.newInputStream(TEST_YAML_TEMPLATE));

            Assertions.assertEquals("test text", storage.read("key1.text", String.class));
            Assertions.assertEquals(10, storage.read("key1.num", Integer.class));
            Assertions.assertEquals("test key", storage.read("key1.prop.key", String.class));
            Assertions.assertEquals(true, storage.read("key1.prop.val", Boolean.class));

            Assertions.assertEquals("test text 2", storage.read("key2.text", String.class));
            Assertions.assertEquals("test text 3", storage.read("key3.text", String.class));
        });
    }

}
