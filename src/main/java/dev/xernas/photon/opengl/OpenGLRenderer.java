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
import java.util.*;
import java.util.List;

public class OpenGLRenderer implements IRenderer<GLFramebuffer, GLShader, GLMesh, GLTexture> {

    private final Window window;
    private final boolean vsync;
    private final boolean debug;

    private final List<GLFramebuffer> loadedFramebuffers = new ArrayList<>();
    private final List<GLShader> loadedShaders = new ArrayList<>();
    private final List<GLMesh> loadedMeshes = new ArrayList<>();
    private final List<GLTexture> loadedTextures = new ArrayList<>();

    private final Map<String, GLMesh> modelsToLoadedMeshesMap = new HashMap<>();
    private final Map<String, Integer> meshesPerModelTagCounter = new HashMap<>();

    private final Map<String, GLShader> loadedShadersMap = new HashMap<>();

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
        if (mesh.getModel().usePerspective()) GLUtils.enableBackfaceCulling();
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
    public boolean useTexture(String name, GLTexture texture, int slot, GLShader shader) {
        if (!loadedTextures.contains(texture)) return false; // TODO Better error handling
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
        if (loadedShadersMap.containsKey(shader.getName())) return loadedShadersMap.get(shader.getName());
        GLShader glShader = new GLShader(shader);
        glShader.start();
        loadedShaders.add(glShader);
        loadedShadersMap.put(shader.getName(), glShader);
        return glShader;
    }

    @Override
    public GLMesh loadMesh(Model model) throws PhotonException {
        incrementMeshesPerModelTag(model.getModelTag());
        if (modelsToLoadedMeshesMap.containsKey(model.getModelTag())) return modelsToLoadedMeshesMap.get(model.getModelTag());
        GLMesh glMesh = new GLMesh(model);
        glMesh.start();
        loadedMeshes.add(glMesh);
        modelsToLoadedMeshesMap.put(model.getModelTag(), glMesh);
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
        for (Map.Entry<String, GLMesh> model : new HashSet<>(modelsToLoadedMeshesMap.entrySet())) {
            if (mesh.equals(model.getValue())) {
                if (decreaseMeshesPerModelTag(model.getKey())) return true;
                modelsToLoadedMeshesMap.remove(model.getKey());
            }
        }
        boolean hadMesh = loadedMeshes.remove(mesh);
        if (hadMesh) mesh.dispose();
        return hadMesh;
    }

    @Override
    public boolean unloadShader(GLShader shader) throws PhotonException {
        for (Map.Entry<String, GLShader> namedShader : new HashSet<>(loadedShadersMap.entrySet())) {
            if (shader.equals(namedShader.getValue())) loadedShadersMap.remove(namedShader.getKey());
        }
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
        modelsToLoadedMeshesMap.clear();
        meshesPerModelTagCounter.clear();
        loadedShadersMap.clear();
    }

    private void incrementMeshesPerModelTag(String modelTag) {
        Integer count = meshesPerModelTagCounter.get(modelTag);
        if (count == null) count = 0;
        meshesPerModelTagCounter.put(modelTag, count + 1);
    }

    private boolean decreaseMeshesPerModelTag(String modelTag) {
        Integer count = meshesPerModelTagCounter.get(modelTag);
        if (count == null) count = 0;
        if (count == 0) return false;
        meshesPerModelTagCounter.put(modelTag, count - 1);
        return meshesPerModelTagCounter.get(modelTag) != 0;
    }

}
