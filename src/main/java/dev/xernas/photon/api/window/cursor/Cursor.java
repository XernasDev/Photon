package dev.xernas.photon.api.window.cursor;

import dev.xernas.photon.api.PhotonLogic;
import dev.xernas.photon.exceptions.PhotonException;
import org.lwjgl.glfw.GLFW;

public class Cursor implements PhotonLogic {

    private long handle;

    private final CursorShape shape;

    public Cursor(CursorShape shape) {
        this.shape = shape;
    }

    @Override
    public void start() throws PhotonException {
        handle = GLFW.glfwCreateStandardCursor(shape.getGlfwShape());
    }

    @Override
    public void dispose() throws PhotonException {
        GLFW.glfwDestroyCursor(handle);
    }

    public long getHandle() {
        return handle;
    }

    public CursorShape getShape() {
        return shape;
    }
}
