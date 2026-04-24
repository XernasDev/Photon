package dev.xernas.photon;

import dev.xernas.photon.api.framebuffer.IFramebuffer;
import dev.xernas.photon.api.model.IMesh;
import dev.xernas.photon.api.IRenderer;
import dev.xernas.photon.api.shader.IShader;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.api.texture.Texture;
import dev.xernas.photon.opengl.GLTexture;
import dev.xernas.photon.opengl.OpenGLRenderer;
import dev.xernas.photon.api.window.Window;

import java.util.Locale;

public enum Library {

    VULKAN_1_0("1.0"),
    VULKAN_1_1("1.1"),
    VULKAN_1_2("1.2"),
    VULKAN_1_3("1.3"),
    VULKAN_1_4("1.4"),
    OPENGL_4_5("4.5"),
    OPENGL_4_6("4.6");

    private final String version;

    Library(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public boolean higher(Library other) {
        if (!isSameAPI(other)) throw new IllegalArgumentException("Cannot compare versions of different APIs");
        int thisVersion = Integer.parseInt(this.version.replace(".", ""));
        int otherVersion = Integer.parseInt(other.version.replace(".", ""));
        return thisVersion > otherVersion;
    }

    public boolean higherOrEquals(Library other) {
        if (!isSameAPI(other)) throw new IllegalArgumentException("Cannot compare versions of different APIs");
        int thisVersion = Integer.parseInt(this.version.replace(".", ""));
        int otherVersion = Integer.parseInt(other.version.replace(".", ""));
        return thisVersion >= otherVersion;
    }

    public boolean isSameAPI(Library other) {
        return (this.isVulkan() && other.isVulkan()) || (this.isOpenGL() && other.isOpenGL());
    }

    public boolean isVulkan() {
        return name().toLowerCase(Locale.ROOT).startsWith("vulkan");
    }

    public boolean isOpenGL() {
        return name().toLowerCase(Locale.ROOT).startsWith("opengl");
    }

    public IRenderer<? extends IFramebuffer, ? extends IShader, ? extends IMesh, ? extends ITexture> createRenderer(Window window, boolean vsync, boolean debug) {
        if (isVulkan()) {
            throw new IllegalStateException("Vulkan is in development and not yet supported.");
            // return new VulkanRenderer(window, vsync, debug);
        } else if (isOpenGL()) {
            return new OpenGLRenderer(window, vsync, debug);
        }
        throw new IllegalArgumentException("Unsupported library: " + name());
    }

}
