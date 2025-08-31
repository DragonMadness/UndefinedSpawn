package com.github.dragonmadness.undefinedSpawn.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class Teleportation {
    private final String player;
    private final Long time;
    private final SimpleLocation origin;

    private boolean returned;

    public Teleportation(String player, Long time, boolean returned, SimpleLocation origin) {
        this.player = player;
        this.time = time;
        this.returned = returned;
        this.origin = origin;
    }

    @JsonCreator
    public Teleportation(
            @JsonProperty("player") String player,
            @JsonProperty("time") Long time,
            @JsonProperty("origin") SimpleLocation origin) {
        this(player, time, false, origin);
    }
}
