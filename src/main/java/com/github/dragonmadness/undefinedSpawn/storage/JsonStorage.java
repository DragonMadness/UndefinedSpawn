package com.github.dragonmadness.undefinedSpawn.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dragonmadness.undefinedSpawn.util.IteratorMisc;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JsonStorage implements Storage {
    protected final Path path;

    protected File file;

    public JsonStorage load() throws IOException {
        if (Files.exists(path)) {
            file = path.toFile();
        } else {
            Files.createDirectories(path.getParent());
            file = Files.createFile(path).toFile();
        }
        return this;
    }

    public boolean isLoaded() {
        return file != null;
    }

    @Override
    public JsonNodeType getFieldType(String location) throws IOException {
        ObjectMapper mapper = constructMapper();
        JsonNode root = mapper.readTree(file);
        return readNode(root, location).getNodeType();
    }

    @Override
    public <T> T read(String location, Class<T> type) throws IOException {
        String[] nodes = location.split("\\.");

        ObjectMapper mapper = constructMapper();
        JsonNode node = mapper.readTree(file);
        for (int i = 0; i < nodes.length; i++) {
            node = node.get(nodes[i]);
            if (node == null) {
                throw new IOException("Node %s doesn't exist in file %s".formatted(nodes[i], path.getFileName()));
            }
        }

        return mapper.treeToValue(node, type);
    }

    @Override
    public <T> List<T> readList(String location, Class<T> elementType) throws IOException {
        ObjectMapper mapper = constructMapper();
        JsonNode node = readNode(mapper.readTree(file), location, true);

        List<T> results = new ArrayList<>();
        Iterator<JsonNode> elements = node.elements();
        while (elements.hasNext()) {
            JsonNode arrayNode = elements.next();
            results.add(mapper.treeToValue(arrayNode, elementType));
        }
        return results;
    }

    @Override
    public <T> void write(String location, T value) throws IOException {
        ObjectMapper mapper = constructMapper();
        JsonNode newNode = mapper.valueToTree(value);

        JsonNode root = mapper.readTree(file);
        root = putByComplexKey(root, location, newNode);
        Files.writeString(path, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
    }

    public <T> void writeToList(String location, T... values) throws IOException {
        ObjectMapper mapper = constructMapper();
        List<JsonNode> newNodes = Arrays.stream(values).map(n -> (JsonNode) mapper.valueToTree(n)).toList();

        JsonNode root = mapper.readTree(file);
        ArrayNode arrayNode;
        try {
            arrayNode = (ArrayNode) readNode(root, location, true);
        } catch (IOException ignored) {
            root = getFixedNodeTree(root, location);
            arrayNode = new ArrayNode(mapper.getNodeFactory());
        }
        arrayNode.addAll(newNodes);
        Files.writeString(path, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
    }

    protected JsonNode readNode(JsonNode root, String location, boolean isArray) throws IOException {
        List<String> nodes = getNodeNames(location);
        JsonNode node = root;
        for (int i = 0; i < nodes.size(); i++) {
            node = node.get(nodes.get(i));
            if (node == null) {
                throw new IOException("Node %s doesn't exist in file %s".formatted(nodes.get(i), path.getFileName()));
            }
        }

        if (isArray && !node.isArray()) {
            throw new IOException("Node %s is not an array!");
        }
        return node;
    }

    protected JsonNode readNode(JsonNode root, String location) throws IOException {
        return readNode(root, location, false);
    }

    protected ObjectNode putByComplexKey(JsonNode root, String key, JsonNode value) throws IOException {
        ObjectNode usedRoot;
        if (root.getNodeType() != JsonNodeType.OBJECT) {
            usedRoot = getFixedNodeTree(root, key);
        } else {
            usedRoot = (ObjectNode) root;
        }
        if (key.contains(".")) {
            JsonNode parentNode = readNode(usedRoot, getNodeParentLocation(key));
            if (parentNode.getNodeType() != JsonNodeType.OBJECT) {
                usedRoot = getFixedNodeTree(usedRoot, key);
                parentNode = readNode(usedRoot, getNodeParentLocation(key));
            }
            ((ObjectNode) parentNode).set(getNodeNames(key).getLast(), value);
        } else {
            usedRoot.set(key, value);
        }
        return usedRoot;
    }

    protected ObjectNode getFixedNodeTree(JsonNode root, String key) {
        ObjectMapper mapper = constructMapper();
        ObjectNode newRoot;
        if (root.getNodeType() != JsonNodeType.OBJECT) {
            newRoot = new ObjectNode(mapper.getNodeFactory());
        } else {
            newRoot = (ObjectNode) root;
        }

        ObjectNode currentNode = newRoot;
        List<String> nodes = getNodeNames(key);
        nodes.removeLast();
        for (String nodeName : nodes) {
            if (currentNode.has(nodeName) && currentNode.get(nodeName).getNodeType() == JsonNodeType.OBJECT) {
                currentNode = (ObjectNode) currentNode.get(nodeName);
                continue;
            }

            ObjectNode nextNode = new ObjectNode(mapper.getNodeFactory());
            currentNode.set(nodeName, nextNode);
            currentNode = nextNode;
        }

        return newRoot;
    }

    protected ObjectMapper constructMapper() {
        return new ObjectMapper();
    }

    public void addMissingFields(InputStream templateStream) throws IOException {
        ObjectMapper objectMapper = constructMapper();
        JsonNode templateRoot = objectMapper.readTree(templateStream);
        if (templateRoot.getNodeType() != JsonNodeType.OBJECT) {
            return;
        }

        if (Files.readString(path).isEmpty()) {
            Files.writeString(path, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(templateRoot));
            return;
        }

        ObjectNode targetRoot = (ObjectNode) objectMapper.readTree(file);
        List<String> keysToCheck = listKeys(templateRoot); // from the start it's the root keys
        int i = 0;
        while (i < keysToCheck.size()) {
            String key = keysToCheck.get(i);
            JsonNode templateNode = readNode(templateRoot, key);
            JsonNode targetNode;
            try {
                targetNode = readNode(targetRoot, key);
            } catch (IOException e) {
                targetRoot = putByComplexKey(targetRoot, key, templateNode);
                i++;
                continue;
            }

            if (templateNode.getNodeType() == JsonNodeType.OBJECT && templateNode.getNodeType() != targetNode.getNodeType()) {
                targetRoot = putByComplexKey(targetRoot, key, templateNode);
            }

            if (templateNode.getNodeType() == JsonNodeType.OBJECT) {
                listKeys(templateNode).stream().map(n -> "%s.%s".formatted(key, n))
                        .forEach(keysToCheck::add);
            }

            i++;
        }

        Files.writeString(path, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(targetRoot));
    }

    public List<String> listKeys(JsonNode node) {
        return IteratorMisc.asList(node.fieldNames());
    }

    public static List<String> getNodeNames(String location) {
        return Arrays.stream(location.split("\\.")).collect(Collectors.toList());
    }

    public static String getNodeParentLocation(String location) {
        Pattern pattern = Pattern.compile("^(.*)(\\.[^.]+)$");
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }
}
