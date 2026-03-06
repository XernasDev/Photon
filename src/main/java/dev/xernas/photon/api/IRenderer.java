package dev.xernas.photon.api;

import dev.xernas.photon.api.framebuffer.Framebuffer;
import dev.xernas.photon.api.framebuffer.IFramebuffer;
import dev.xernas.photon.api.model.IMesh;
import dev.xernas.photon.api.model.Model;
import dev.xernas.photon.api.shader.IShader;
import dev.xernas.photon.api.shader.Shader;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.api.texture.Texture;
import dev.xernas.photon.exceptions.PhotonException;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public interface IRenderer<F extends IFramebuffer, S extends IShader, M extends IMesh, T extends ITexture> extends PhotonLogic {

    void render(@Nullable F framebuffer, S shader, M mesh, Runnable operations) throws PhotonException;

    default void render(S shader, M mesh, Runnable operations) throws PhotonException {
        render(null, shader, mesh, operations);
    }

    void swapBuffers() throws PhotonException;

    void clear(Color color) throws PhotonException;

    default void clear() throws PhotonException {
        clear(Color.BLACK);
    }

    boolean useTexture(String name, T texture, int slot, S shader) throws PhotonException;

    T loadTexture(Texture texture) throws PhotonException;

    M loadMesh(Model model) throws PhotonException;

    S loadShader(Shader shader) throws PhotonException;

    F loadFramebuffer(Framebuffer framebuffer) throws PhotonException;

    boolean unloadTexture(T texture) throws PhotonException;

    boolean unloadMesh(M mesh) throws PhotonException;

    boolean unloadShader(S shader) throws PhotonException;

    boolean unloadFramebuffer(F framebuffer) throws PhotonException;

}
