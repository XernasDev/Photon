package dev.xernas.photon.api.window.input;

import dev.xernas.photon.api.window.Window;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class Input {

    private static Window window;
    private static Boolean azerty;
    private static final Map<Key, Action> keyMap = new HashMap<>();
    private static final Mouse mouse = new Mouse(0, 0);
    private static final Mouse absoluteMouse = new Mouse(0, 0);

    public static void setWindow(Window window) {
        Input.window = window;
    }

    public static void setAzerty(boolean azerty) {
        Input.azerty = azerty;
    }

    public static boolean isInitialized() {
        return window != null && azerty != null;
    }

    public static void updateInput() {
        if (keyMap.isEmpty()) return;
        keyMap.clear();
        resetScrollDelta();
    }

    public static Action getKeyAction(Key key) {
        return keyMap.getOrDefault(key, Action.IDLE);
    }

    public static boolean isPressing(Key key) {
        if (key.isMouseButton()) return GLFW.glfwGetMouseButton(window.getHandle(), key.getQwerty()) == GLFW.GLFW_PRESS;
        else return GLFW.glfwGetKey(window.getHandle(), key.getQwerty()) == GLFW.GLFW_PRESS;
    }

    public static boolean hasReleased(Key key) {
        return getKeyAction(key) == Action.RELEASE;
    }

    public static boolean hasHold(Key key) {
        return getKeyAction(key) == Action.HOLD;
    }

    public static boolean hasPressed(Key button) {
        return getKeyAction(button) == Action.PRESS;
    }

    public static void setKeyAction(Key key, Action action) {
        keyMap.put(key, action);
    }

    public static void setMousePosition(double x, double y) {
        mouse.set((float) x, (float) y);
    }

    public static void setScrollDelta(float x, float y) {
        mouse.setScrollDelta(x, y);
    }

    public static void resetScrollDelta() {
        mouse.setScrollDelta(0, 0);
    }

    public static void setAbsoluteMousePosition(double x, double y) {
        absoluteMouse.set((float) x, (float) y);
    }

    public static boolean isAzerty() {
        return azerty;
    }

    public static Window getWindow() {
        return window;
    }

    public static Mouse getMouse() {
        return mouse;
    }

    public static Mouse getAbsoluteMouse() {
        return absoluteMouse;
    }
}