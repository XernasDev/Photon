package dev.xernas.photon.api.window;

import dev.xernas.photon.PhotonAPI;
import dev.xernas.photon.api.PhotonLogic;
import dev.xernas.photon.api.IRenderer;
import dev.xernas.photon.api.framebuffer.IFramebuffer;
import dev.xernas.photon.api.model.IMesh;
import dev.xernas.photon.api.shader.IShader;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.api.window.cursor.Cursor;
import dev.xernas.photon.api.window.cursor.CursorShape;
import dev.xernas.photon.api.window.input.Action;
import dev.xernas.photon.api.window.input.Input;
import dev.xernas.photon.api.window.input.Key;
import dev.xernas.photon.exceptions.PhotonException;
import dev.xernas.photon.opengl.GLUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

public class Window implements PhotonLogic {

    private final boolean resizable;

    private final String defaultTitle;
    private final Input input;
    private String title;
    private int width;
    private int height;
    private Cursor currentCursor;

    private long handle;
    private GLFWErrorCallback errorCallback;
    private boolean framebufferResized = false;
    private boolean cursorLocked = false;

    public Window(String title, int width, int height) {
        this(true, title, width, height);
    }

    public Window(boolean resizable, String title, int width, int height) {
        this.resizable = resizable;
        this.defaultTitle = title;
        this.title = title;
        this.width = width;
        this.height = height;
        this.input = new Input(this, true);
    }

    @Override
    public void start() throws PhotonException {
        // Initialize GLFW
        if (!PhotonAPI.isInitialized()) throw new PhotonException("PhotonAPI not initialized");
        if (PhotonAPI.isDebug()) {
            errorCallback = GLFWErrorCallback.createPrint(System.err);
            errorCallback.set();
        }
        if (!GLFW.glfwInit()) throw new PhotonException("Failed to initialize GLFW");

        // Configure GLFW
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, PhotonAPI.getLibrary().isVulkan() ? GLFW.GLFW_NO_API : GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_DOUBLEBUFFER, GLFW.GLFW_TRUE);
        if (PhotonAPI.getLibrary().isOpenGL()) {
            String[] version = PhotonAPI.getLibrary().getVersion().split("\\.");
            int major = Integer.parseInt(version[0]);
            int minor = Integer.parseInt(version[1]);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, major);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, minor);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
            if (PhotonAPI.isDebug()) GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        }

        // Create the window
        handle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) throw new PhotonException("Failed to create GLFW window");


        // Callbacks
        GLFW.glfwSetFramebufferSizeCallback(handle, (window, w, h) -> {
            if (w == 0 || h == 0) return;
            setFramebufferResized(true);
            resize(w, h);
        });
        // Keyboard
        GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> input.setKeyAction(Key.fromCode(key, input.isAzerty()), Action.fromCode(action)));
        // Mouse
        GLFW.glfwSetMouseButtonCallback(handle, (window, button, action, mods) -> input.setKeyAction(Key.fromCode(button, input.isAzerty()), Action.fromCode(action)));
        // Mouse position
        GLFW.glfwSetCursorPosCallback(handle, (window, xpos, ypos) -> {
            input.setMousePosition(xpos, ypos);
            if (cursorLocked) setCursorPosition(width / 2, height / 2);
        });
        // Scroll
        GLFW.glfwSetScrollCallback(handle, (window, xoffset, yoffset) -> {
            input.setScrollDelta((float) xoffset, (float) yoffset);
        });

        // Set default cursor
        currentCursor = new Cursor(CursorShape.ARROW);
        currentCursor.start();
    }

    public void update(IRenderer<? extends IFramebuffer, ? extends IShader, ? extends IMesh, ? extends ITexture> renderer) throws PhotonException {
        renderer.swapBuffers();
        input.updateInput();
        GLFW.glfwPollEvents();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer xCursor = stack.mallocDouble(1);
            DoubleBuffer yCursor = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(handle, xCursor, yCursor);
            IntBuffer xWindow = stack.mallocInt(1);
            IntBuffer yWindow = stack.mallocInt(1);
            GLFW.glfwGetWindowPos(handle, xWindow, yWindow);
            input.setAbsoluteMousePosition(xWindow.get(0) + (float) xCursor.get(0), yWindow.get(0) + (float) yCursor.get(0));
        }
    }

    public void show() {
        GLFW.glfwShowWindow(handle);
        GLFW.glfwRestoreWindow(handle);
        // Center the window
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(handle, pWidth, pHeight);
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            if (vidMode != null) {
                GLFW.glfwSetWindowPos(
                        handle,
                        (vidMode.width() - pWidth.get(0)) / 2,
                        (vidMode.height() - pHeight.get(0)) / 2
                );
            }
        }
    }

    public void close() {
        GLFW.glfwSetWindowShouldClose(handle, true);
    }

    public void hide() {
        GLFW.glfwHideWindow(handle);
    }

    public void minimize() {
        GLFW.glfwIconifyWindow(handle);
    }

    public boolean isOpen() {
        return !GLFW.glfwWindowShouldClose(handle);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        GLUtils.viewport(this);
    }

    public void setTitle(String title) {
        this.title = title;
        GLFW.glfwSetWindowTitle(handle, title);
    }

    public String getTitle() {
        return title;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public long getHandle() {
        return handle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Input getInput() {
        return input;
    }

    public void setCursorPosition(int x, int y) {
        GLFW.glfwSetCursorPos(handle, x, y);
    }

    public void disableCursor() {
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    public void hideCursor() {
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
    }

    public void showCursor() {
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }

    public void setCursorLocked(boolean locked) {
        cursorLocked = locked;
    }

    public void setCursorShape(CursorShape shape) throws PhotonException {
        currentCursor.dispose();
        currentCursor = new Cursor(shape);
        currentCursor.start();
        GLFW.glfwSetCursor(handle, currentCursor.getHandle());
    }

    public Vector2f getMaxBounds() {
        float aspectRatio;
        float worldWidth, worldHeight;

        if (width >= height) {
            // Horizontal screen
            aspectRatio = (float) width / height;
            worldWidth = 2.0f * aspectRatio;
            worldHeight = 2.0f;
        } else {
            // Vertical screen
            aspectRatio = (float) height / width;
            worldWidth = 2.0f;
            worldHeight = 2.0f * aspectRatio;
        }

        return new Vector2f(worldWidth, worldHeight);
    }

    public Vector3f pixelToWorldCoords(int pixelX, int pixelY) {
        return pixelToWorldCoords(pixelX, pixelY, 0.0f);
    }

    public Vector3f pixelToWorldCoords(int pixelX, int pixelY, float z) {
        float aspectRatio;
        float worldX, worldY;

        if (width >= height) {
            // Horizontal screen
            aspectRatio = (float) width / height;
            worldX = ((float) pixelX / width) * (2.0f * aspectRatio) - aspectRatio;
            worldY = 1.0f - ((float) pixelY / height) * 2.0f;
        } else {
            // Vertical screen
            aspectRatio = (float) height / width;
            worldX = ((float) pixelX / width) * 2.0f - 1.0f;
            worldY = 1.0f - ((float) pixelY / height) * (2.0f * aspectRatio);
        }

        return new Vector3f(worldX, worldY, z);
    }

    public boolean framebufferResized() {
        return framebufferResized;
    }

    public void setFramebufferResized(boolean framebufferResized) {
        this.framebufferResized = framebufferResized;
    }

    @Override
    public void dispose() throws PhotonException {
        if (handle != MemoryUtil.NULL){
            close();
            currentCursor.dispose();
            Callbacks.glfwFreeCallbacks(handle);
            GLFW.glfwDestroyWindow(handle);
        }
        GLFW.glfwTerminate();
        if (errorCallback != null)  {
            errorCallback.free();
            errorCallback = null;
        }
    }
}
