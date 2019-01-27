package com.neatsketch.mycozyplaceserver;

import java.util.*;

public class WorldMap {

    private static final int CHUNK_SIZE = 16;

    private static Map<String, Player> loggedPlayerMap = new HashMap<>();

    private static Map<AbstractMap.SimpleImmutableEntry<Integer, Integer>, LinkedList<WorldMapEntity>> entityMap = new HashMap<>();

    static String loginPlayer(String username, String password) {
        Player player = loggedPlayerMap.get(username);
        if (player == null) {
            player = new Player(username);
            spawnPlayer(player);
            loggedPlayerMap.put(username, player);
        }
        String authToken = player.login(password);
        if (authToken != null) {
            player.heartbeat();
        }
        return authToken;
    }

    static Player getPlayer(String username) {
        return loggedPlayerMap.get(username);
    }

    private static void spawnPlayer(Player player) {
        updatePlayer(
                player,
                0f, 0f,
                0f, 0f,
                null, null, null,
                null, null, null);
    }

    static void updatePlayer(
            Player player,
            float positionX, float positionZ,
            float velocityX, float velocityZ,
            String accHead, String accNeck, String accButt,
            String accHead2, String accEyes, String accMouth) {
        player.setPositionAndVelocity(positionX, positionZ, velocityX, velocityZ);

        player.accessoryHead = accHead;
        player.accessoryNeck = accNeck;
        player.accessoryButt = accButt;
        player.accessoryHead2 = accHead2;
        player.accessoryEyes = accEyes;
        player.accessoryMouth = accMouth;

        int blockX = Math.round(positionX);
        int blockZ = Math.round(positionZ);

        AbstractMap.SimpleImmutableEntry<Integer, Integer> chunk = getChunkByBlockPosition(blockX, blockZ);

        int chunkX = chunk.getKey();
        int chunkZ = chunk.getValue();

        if (player.spawned) {
            if (chunkX != player.chunkX || chunkZ != player.chunkZ) {
                removePlayerFromChunk(player);
                addPlayerToChunk(player, chunk);
            }
        }
        else
        {
            addPlayerToChunk(player, chunk);
            player.spawned = true;
        }

        player.heartbeat();
    }

    private static void removePlayerFromChunk(Player player) {
        AbstractMap.SimpleImmutableEntry<Integer, Integer> chunk = new AbstractMap.SimpleImmutableEntry<>(player.chunkX, player.chunkZ);
        getEntitiesInChunk(chunk).remove(player);

        System.out.println(String.format("Player %s is removed from chunk [%d, %d]", player.username, chunk.getKey(), chunk.getValue()));
    }

    private static void addPlayerToChunk(Player player, AbstractMap.SimpleImmutableEntry<Integer, Integer> chunk) {
        getEntitiesInChunk(chunk).add(player);

        int chunkX = chunk.getKey();
        int chunkZ = chunk.getValue();

        player.chunkX = chunkX;
        player.chunkZ = chunkZ;

        System.out.println(String.format("Player %s is ADDED to chunk [%d, %d]", player.username, chunk.getKey(), chunk.getValue()));
    }

    //private int findIndexOfPlayerInEntityList()

    static void setBlock(int blockX, int blockZ, int blockType) {
        AbstractMap.SimpleImmutableEntry<Integer, Integer> chunk = getChunkByBlockPosition(blockX, blockZ);
        LinkedList<WorldMapEntity> entitiesInChunk = getEntitiesInChunk(chunk);
        boolean found = false;
        for (WorldMapEntity entity : entitiesInChunk) {
            if (entity instanceof Block) {
                Block block = (Block)entity;
                if (block.positionX == blockX && block.positionZ == blockZ) {
                    block.blockType = blockType;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            entitiesInChunk.add(new Block(blockX, blockZ, blockType));
        }
    }

    static void setFurniture(int blockX, int blockZ, int furnitureType, int rotation) {
        AbstractMap.SimpleImmutableEntry<Integer, Integer> chunk = getChunkByBlockPosition(blockX, blockZ);
        LinkedList<WorldMapEntity> entitiesInChunk = getEntitiesInChunk(chunk);
        boolean found = false;
        for (WorldMapEntity entity : entitiesInChunk) {
            if (entity instanceof Furniture) {
                Furniture furniture = (Furniture)entity;
                if (furniture.positionX == blockX && furniture.positionZ == blockZ) {
                    furniture.furnitureType = furnitureType;
                    furniture.rotation = rotation;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            entitiesInChunk.add(new Furniture(blockX, blockZ, furnitureType, rotation));
        }
    }

    static AbstractMap.SimpleImmutableEntry<Integer, Integer> getChunkByBlockPosition(int blockX, int blockZ) {
        int x = (blockX >= 0) ? (blockX / CHUNK_SIZE) : (blockX / CHUNK_SIZE - 1);
        int z = (blockZ >= 0) ? (blockZ / CHUNK_SIZE) : (blockZ / CHUNK_SIZE - 1);
        return new AbstractMap.SimpleImmutableEntry<>(x, z);
    }

    private static LinkedList<WorldMapEntity> getEntitiesInChunk(AbstractMap.SimpleImmutableEntry<Integer, Integer> chunk) {
        LinkedList<WorldMapEntity> entities = entityMap.get(chunk);
        if (entities == null) {
            entities = new LinkedList<>();
            entityMap.put(chunk, entities);
        }
        return entities;
    }

    static ArrayList<LinkedList<WorldMapEntity>> getEntitiesInChunkAndItsNeighbors(AbstractMap.SimpleImmutableEntry<Integer, Integer> chunk) {
        ArrayList<LinkedList<WorldMapEntity>> entities = new ArrayList<LinkedList<WorldMapEntity>>();
        int x = chunk.getKey();
        int z = chunk.getValue();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                AbstractMap.SimpleImmutableEntry<Integer, Integer> currentChunk;
                if (i == 0 && j == 0) {
                    currentChunk = chunk;
                } else {
                    currentChunk = new AbstractMap.SimpleImmutableEntry<>(x + j, z + i);
                }
                LinkedList<WorldMapEntity> entitiesInCurrentChunk = getEntitiesInChunk(currentChunk);
                entities.add(entitiesInCurrentChunk);
            }
        }
        return entities;
    }



}
