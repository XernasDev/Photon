package dev.xernas.photon.api;

import org.lwjgl.opengl.GL45;

public enum DrawMode {

    TRIANGLES,
    TRIANGLE_FAN,
    TRIANGLE_STRIP,
    LINES,
    POINTS;

    public int toGLDrawMode() {
        return switch (this) {
            case TRIANGLES -> GL45.GL_TRIANGLES;
            case TRIANGLE_FAN -> GL45.GL_TRIANGLE_FAN;
            case TRIANGLE_STRIP -> GL45.GL_TRIANGLE_STRIP;
            case LINES -> GL45.GL_LINES;
            case POINTS -> GL45.GL_POINTS;
        };
    }

}
