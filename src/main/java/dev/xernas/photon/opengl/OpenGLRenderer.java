package dev.xernas.photon.opengl;

import dev.xernas.photon.PhotonAPI;
import dev.xernas.photon.api.IRenderer;
import dev.xernas.photon.api.framebuffer.Framebuffer;
import dev.xernas.photon.api.model.Model;
import dev.xernas.photon.api.shader.Shader;
import dev.xernas.photon.api.texture.Texture;
import dev.xernas.photon.exceptions.PhotonException;
import dev.xernas.photon.api.window.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OpenGLRenderer implements IRenderer<GLFramebuffer, GLShader, GLMesh, GLTexture> {

    private final Window window;
    private final boolean vsync;
    private final boolean debug;

    private final List<GLFramebuffer> loadedFramebuffers = new ArrayList<>();
    private final List<GLShader> loadedShaders = new ArrayList<>();
    private final List<GLMesh> loadedMeshes = new ArrayList<>();
    private final List<GLTexture> loadedTextures = new ArrayList<>();

    public OpenGLRenderer(Window window, boolean vsync, boolean debug) {
        this.window = window;
        this.vsync = vsync;
        this.debug = debug;
    }

    @Override
    public void render(GLFramebuffer framebuffer, GLShader shader, GLMesh mesh, Runnable operations) throws PhotonException {
        resizeFramebuffers();
        if (!((framebuffer == null || loadedFramebuffers.contains(framebuffer)) && loadedShaders.contains(shader) && loadedMeshes.contains(mesh)))
            throw new PhotonException("Attempted to render with an unloaded resource");
        if (framebuffer == null) GLFramebuffer.bindDefault();
        else framebuffer.bind();
        // Binds
        shader.bind();
        if (mesh.getModel().is3D()) GLUtils.enableBackfaceCulling();
        else GLUtils.disableBackfaceCulling();

        mesh.bind();
        // Operations
        operations.run();
        // Draw call
        GLUtils.draw(0, mesh.getVertexCount());
    }

    @Override
    public void swapBuffers() throws PhotonException {
        GLFW.glfwSwapBuffers(window.getHandle());
    }

    @Override
    public void clear(Color color) {
        GLUtils.clear(color);
    }

    @Override
    public void start() throws PhotonException {
        if (!PhotonAPI.isInitialized()) throw new PhotonException("PhotonAPI not initialized");
        GLFW.glfwMakeContextCurrent(window.getHandle());
        GLFW.glfwSwapInterval(vsync ? 1 : 0);
        GL.createCapabilities();
        GLUtils.viewport(window);
        GLUtils.enableDepthTest(); // Enable depth testing by default (TODO: Make configurable)
        if (debug) {
            System.out.println("[Photon] OpenGL Starting with Renderer: " + GLUtils.getRendererInfo());
            GLUtils.setupDebugMessageCallback();
        }
    }

    private void resizeFramebuffers() throws PhotonException {
        if (!window.framebufferResized()) return;
        for (GLFramebuffer framebuffer : loadedFramebuffers) framebuffer.resize(window.getWidth(), window.getHeight());
        window.setFramebufferResized(false);
    }

    @Override
    public boolean useTexture(String name, GLTexture texture, int slot, GLShader shader) throws PhotonException {
        if (loadedTextures.contains(texture)) throw new PhotonException("Attempted to use an unloaded texture");
        texture.bind(slot);
        return shader.setUniform(name, slot) != null;
    }

    @Override
    public GLFramebuffer loadFramebuffer(Framebuffer framebuffer) throws PhotonException {
        GLFramebuffer glFramebuffer = new GLFramebuffer(window.getWidth(), window.getHeight(), framebuffer.getAttachments());
        glFramebuffer.start();
        loadedFramebuffers.add(glFramebuffer);
        return glFramebuffer;
    }

    @Override
    public GLShader loadShader(Shader shader) throws PhotonException {
        GLShader glShader = new GLShader(shader);
        glShader.start();
        loadedShaders.add(glShader);
        return glShader;
    }

    @Override
    public GLMesh loadMesh(Model model) throws PhotonException {
        GLMesh glMesh = new GLMesh(model);
        glMesh.start();
        loadedMeshes.add(glMesh);
        return glMesh;
    }

    @Override
    public GLTexture loadTexture(Texture texture) throws PhotonException {
        GLTexture glTexture = new GLTexture(texture);
        glTexture.start();
        loadedTextures.add(glTexture);
        return glTexture;
    }

    @Override
    public boolean unloadTexture(GLTexture texture) throws PhotonException {
        boolean hadTexture = loadedTextures.remove(texture);
        if (hadTexture) texture.dispose();
        return hadTexture;
    }

    @Override
    public boolean unloadMesh(GLMesh mesh) throws PhotonException {
        boolean hadMesh = loadedMeshes.remove(mesh);
        if (hadMesh) mesh.dispose();
        return hadMesh;
    }

    @Override
    public boolean unloadShader(GLShader shader) throws PhotonException {
        boolean hadShader = loadedShaders.remove(shader);
        if (hadShader) shader.dispose();
        return hadShader;
    }

    @Override
    public boolean unloadFramebuffer(GLFramebuffer framebuffer) throws PhotonException {
        boolean hadFramebuffer = loadedFramebuffers.remove(framebuffer);
        if (hadFramebuffer) framebuffer.dispose();
        return hadFramebuffer;
    }

    @Override
    public void dispose() throws PhotonException {
        for (GLTexture texture : loadedTextures) texture.dispose();
        for (GLMesh mesh : loadedMeshes) mesh.dispose();
        for (GLShader shader : loadedShaders) shader.dispose();
        for (GLFramebuffer framebuffer : loadedFramebuffers) framebuffer.dispose();
        loadedTextures.clear();
        loadedMeshes.clear();
        loadedShaders.clear();
        loadedFramebuffers.clear();
    }
}
