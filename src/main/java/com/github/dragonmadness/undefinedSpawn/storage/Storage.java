package com.github.dragonmadness.undefinedSpawn.storage;

import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface Storage {

    boolean isLoaded();
    JsonNodeType getFieldType(String location) throws IOException;
    <T> T read(String location, Class<T> type) throws IOException;
    <T> List<T> readList(String location, Class<T> elementType) throws IOException;
    <T> void write(String location, T value) throws IOException;
    <T> void writeToList(String location, T... values) throws IOException;
    void addMissingFields(InputStream template) throws IOException;

}
