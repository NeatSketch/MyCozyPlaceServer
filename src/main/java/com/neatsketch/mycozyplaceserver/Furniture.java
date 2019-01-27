package com.neatsketch.mycozyplaceserver;

public class Furniture extends WorldMapEntity {

    int rotation;
    int furnitureType;

    int positionX;
    int positionZ;

    Furniture(int positionX, int positionZ, int furnitureType, int rotation) {
        this.furnitureType = furnitureType;
        this.rotation = rotation;
        this.positionX = positionX;
        this.positionZ = positionZ;
    }

}
