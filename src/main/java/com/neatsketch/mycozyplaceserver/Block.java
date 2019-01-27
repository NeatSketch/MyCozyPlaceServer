package com.neatsketch.mycozyplaceserver;

public class Block extends WorldMapEntity {

    int blockType;

    int positionX;
    int positionZ;

    Block(int positionX, int positionZ, int blockType) {
        this.blockType = blockType;
        this.positionX = positionX;
        this.positionZ = positionZ;
    }

}
