package com.github.dragonmadness.undefinedSpawn.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.nio.file.Path;

public class YamlStorage extends JsonStorage {

    public YamlStorage(Path path) {super(path);}

    @Override
    protected ObjectMapper constructMapper() {
        return new ObjectMapper(new YAMLFactory());
    }
}
