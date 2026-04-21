package dev.xernas.photon.opengl;

import dev.xernas.photon.api.window.Window;
import dev.xernas.photon.exceptions.GLException;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLDebugMessageCallback;

import java.awt.*;

public class GLUtils {

    private static boolean depthTestEnabled = false;
    private static boolean backfaceCullingEnabled = false;

    public static void draw(int first, int count) {
        if (OpenGLConstants.DRAWING_METHOD.equals(OpenGLConstants.DrawingMethod.ARRAY)) GL45.glDrawArrays(OpenGLConstants.DRAW_MODE.toOpenGLConstant(), first, count);
        else GL45.glDrawElements(OpenGLConstants.DRAW_MODE.toOpenGLConstant(), count, GL45.GL_UNSIGNED_INT, first);
    }

    public static void clear() {
        GL45.glClear(GL45.GL_COLOR_BUFFER_BIT | GL45.GL_DEPTH_BUFFER_BIT);
    }

    public static void clear(Color color) {
        GLFramebuffer.bindDefault();
        if (color != null) {
            GL45.glClearColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            GL45.glClearDepth(1.0f);
        }
        clear();
    }

    public static void clear(GLFramebuffer framebuffer, Color color) {
        framebuffer.bind();
        GL45.glClearNamedFramebufferfv(framebuffer.getFramebufferId(), GL45.GL_COLOR, 0, new float[] {
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        });
        // Clear depth buffer
        GL45.glClearNamedFramebufferfv(framebuffer.getFramebufferId(), GL45.GL_DEPTH, 0, new float[] {1.0f});
    }

    public static void viewport(Window window) {
        viewport(window.getWidth(), window.getHeight());
    }

    public static void viewport(int width, int height) {
        GL45.glViewport(0, 0, width, height);
    }

    public static void enableDepthTest() {
        if (depthTestEnabled) return;
        GL45.glEnable(GL45.GL_DEPTH_TEST);
        GL45.glDepthMask(true);
        depthTestEnabled = true;
    }

    public static void disableDepthTest() {
        if (!depthTestEnabled) return;
        GL45.glDisable(GL45.GL_DEPTH_TEST);
        depthTestEnabled = false;
    }

    public static void enableBackfaceCulling() {
        if (backfaceCullingEnabled) return;
        GL45.glEnable(GL45.GL_CULL_FACE);
        backfaceCullingEnabled = true;
    }

    public static void disableBackfaceCulling() {
        if (!backfaceCullingEnabled) return;
        GL45.glDisable(GL45.GL_CULL_FACE);
        backfaceCullingEnabled = false;
    }

    public static String getError(int error) {
        return switch (error) {
            case GL45.GL_NO_ERROR -> "No error";
            case GL45.GL_INVALID_ENUM -> "Invalid enum";
            case GL45.GL_INVALID_VALUE -> "Invalid value";
            case GL45.GL_INVALID_OPERATION -> "Invalid operation";
            case GL45.GL_STACK_OVERFLOW -> "Stack overflow";
            case GL45.GL_STACK_UNDERFLOW -> "Stack underflow";
            case GL45.GL_OUT_OF_MEMORY -> "Out of memory";
            case GL45.GL_INVALID_FRAMEBUFFER_OPERATION -> "Invalid framebuffer operation";
            case GL45.GL_FRAMEBUFFER_COMPLETE -> "Frame buffer completed";
            case GL45.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "Frame buffer incomplete attachment";
            case GL45.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "Frame buffer incomplete missing attachment";
            case GL45.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "Frame buffer incomplete draw buffer";
            case GL45.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "Frame buffer incomplete read buffer";
            case GL45.GL_FRAMEBUFFER_UNSUPPORTED -> "Frame buffer unsupported";
            default -> "Unknown error";
        };
    }

    public static String getRendererInfo() {
        return GL45.glGetString(GL45.GL_RENDERER);
    }

    public static void setupDebugMessageCallback() {
        GL45.glEnable(GL45.GL_DEBUG_OUTPUT);
        GL45.glEnable(GL45.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        // Ignorer les notifications
        GL45.glDebugMessageControl(GL45.GL_DONT_CARE, GL45.GL_DONT_CARE, GL45.GL_DEBUG_SEVERITY_NOTIFICATION, (java.nio.IntBuffer) null, false);

        GLDebugMessageCallback callback = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
            StringBuilder sb = new StringBuilder();
            sb.append("[Photon] New OpenGL Debug Message:\n");
            printDetail(sb, "Source", getSourceString(source));
            printDetail(sb, "Type", getTypeString(type));
            printDetail(sb, "ID", String.valueOf(id));
            printDetail(sb, "Severity", getSeverityString(severity));
            String messageStr = GLDebugMessageCallback.getMessage(length, message);
            printDetail(sb, "Message", messageStr);

            if ((severity == GL45.GL_DEBUG_SEVERITY_HIGH || severity == GL45.GL_DEBUG_SEVERITY_MEDIUM) && (type != GL45.GL_DEBUG_TYPE_PERFORMANCE)) {
                System.err.println(sb);
                new GLException(messageStr).printStackTrace();
            } else System.out.println(sb);
        });
        GL45.glDebugMessageCallback(callback, 0);
    }

    private static void printDetail(StringBuilder sb, String type, String message) {
        sb.append("\t").append(type).append(": ").append(message).append("\n");
    }

    private static String getSourceString(int source) {
        return switch (source) {
            case GL45.GL_DEBUG_SOURCE_API -> "API";
            case GL45.GL_DEBUG_SOURCE_WINDOW_SYSTEM -> "Window System";
            case GL45.GL_DEBUG_SOURCE_SHADER_COMPILER -> "Shader Compiler";
            case GL45.GL_DEBUG_SOURCE_THIRD_PARTY -> "Third Party";
            case GL45.GL_DEBUG_SOURCE_APPLICATION -> "Application";
            case GL45.GL_DEBUG_SOURCE_OTHER -> "Other";
            default -> "Unknown";
        };
    }

    private static String getTypeString(int type) {
        return switch (type) {
            case GL45.GL_DEBUG_TYPE_ERROR -> "Error";
            case GL45.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "Deprecated Behavior";
            case GL45.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "Undefined Behavior";
            case GL45.GL_DEBUG_TYPE_PORTABILITY -> "Portability";
            case GL45.GL_DEBUG_TYPE_PERFORMANCE -> "Performance";
            case GL45.GL_DEBUG_TYPE_MARKER -> "Marker";
            case GL45.GL_DEBUG_TYPE_PUSH_GROUP -> "Push Group";
            case GL45.GL_DEBUG_TYPE_POP_GROUP -> "Pop Group";
            case GL45.GL_DEBUG_TYPE_OTHER -> "Other";
            default -> "Unknown";
        };
    }

    private static String getSeverityString(int severity) {
        return switch (severity) {
            case GL45.GL_DEBUG_SEVERITY_HIGH -> "High";
            case GL45.GL_DEBUG_SEVERITY_MEDIUM -> "Medium";
            case GL45.GL_DEBUG_SEVERITY_LOW -> "Low";
            case GL45.GL_DEBUG_SEVERITY_NOTIFICATION -> "Notification";
            default -> "Unknown";
        };
    }

}
