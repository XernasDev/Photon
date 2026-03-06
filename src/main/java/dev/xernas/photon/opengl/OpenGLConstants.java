package dev.xernas.photon.opengl;

import org.lwjgl.opengl.GL45;

import java.util.ArrayList;

public class OpenGLConstants {

    public static final DrawingMethod DRAWING_METHOD = DrawingMethod.ELEMENT;
    public static final DrawMode DRAW_MODE = DrawMode.TRIANGLES;

    public enum DrawingMethod {
        ARRAY,
        ELEMENT;
    }

    public enum DrawMode {
        TRIANGLES(GL45.GL_TRIANGLES),
        LINES(GL45.GL_LINES),
        POINTS(GL45.GL_POINTS);

        private final int glConstant;

        DrawMode(int glConstant) {
            this.glConstant = glConstant;
        }

        public int toOpenGLConstant() {
            return glConstant;
        }
    }

}
