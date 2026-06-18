package dev.xernas.photon.opengl;

import org.lwjgl.opengl.GL45;

import java.util.ArrayList;

public class OpenGLConstants {

    public static final DrawingMethod DRAWING_METHOD = DrawingMethod.ELEMENT;

    public enum DrawingMethod {
        ARRAY,
        ELEMENT;
    }

}
