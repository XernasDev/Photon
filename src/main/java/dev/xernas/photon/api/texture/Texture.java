package dev.xernas.photon.api.texture;

import java.nio.ByteBuffer;

public class Texture {

    private final int width;
    private final int height;
    private final ByteBuffer data;

    private boolean nearestFiltering;

    public Texture(int width, int height, ByteBuffer data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getData() {
        return data;
    }

    public Texture setNearestFiltering(boolean nearestFiltering) {
        this.nearestFiltering = nearestFiltering;
        return this;
    }

    public boolean isNearestFiltering() {
        return nearestFiltering;
    }
}
