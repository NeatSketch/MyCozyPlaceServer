package com.neatsketch.mycozyplaceserver;

import java.util.Date;
import java.util.UUID;

public class Player extends WorldMapEntity {

    private static final long ONLINE_TIMEOUT = 7000; // 7s

    String username;
    String authToken;

    Date lastUpdateTime;

    float positionX;
    float positionZ;
    float velocityX;
    float velocityZ;

    int chunkX;
    int chunkZ;

    boolean spawned = false;

    Player(String username) {
        this.username = username;
        this.lastUpdateTime = new Date();
    }

    String login(String password) {
        // TODO: password check
        authToken = UUID.randomUUID().toString();
        return authToken;
    }

    void setPositionAndVelocity(float positionX, float positionZ, float velocityX, float velocityZ) {
        this.positionX = positionX;
        this.positionZ = positionZ;
        this.velocityX = velocityX;
        this.velocityZ = velocityZ;
    }

    long getTimeSinceLastUpdate() {
        return (new Date()).getTime() - lastUpdateTime.getTime();
    }

    boolean isOnline() {
        return getTimeSinceLastUpdate() < ONLINE_TIMEOUT;
    }

    void heartbeat() {
        this.lastUpdateTime = new Date();
    }
}
