package dev.xernas.photon.opengl;

import dev.xernas.photon.api.texture.Texture;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.exceptions.PhotonException;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

import java.util.HashMap;
import java.util.Map;

public class GLTexture implements ITexture {

    private static int lastBoundTextureId = 0;
    private static int lastBoundUnit = -1;

    private final Texture image;
    private final boolean hasDefaultData;
    private final GLTextureComponent textureComponent;
    private int textureId;

    public GLTexture(int width, int height, GLTextureComponent textureComponent) {
        this.image = new Texture(width, height, null);
        this.hasDefaultData = false;
        this.textureComponent = textureComponent;
    }

    public GLTexture(Texture image) {
        this.image = image;
        this.hasDefaultData = image.getData() != null;
        this.textureComponent = GLTextureComponent.RGBA;
    }

    @Override
    public void start() throws PhotonException {
        textureId = GL45.glCreateTextures(GL20.GL_TEXTURE_2D);

        int wrapMode = hasDefaultData ? GL20.GL_REPEAT : GL20.GL_CLAMP_TO_EDGE;
        int textureFilter = hasDefaultData ? GL20.GL_LINEAR : (textureComponent == GLTextureComponent.DEPTH ? GL20.GL_NEAREST : GL20.GL_LINEAR);
        if (textureComponent == GLTextureComponent.DEPTH) GL45.glTextureParameteri(textureId, GL20.GL_TEXTURE_COMPARE_MODE, GL20.GL_NONE);
        GL45.glTextureParameteri(textureId, GL20.GL_TEXTURE_WRAP_S, wrapMode);
        GL45.glTextureParameteri(textureId, GL20.GL_TEXTURE_WRAP_T, wrapMode);
        GL45.glTextureParameteri(textureId, GL20.GL_TEXTURE_MIN_FILTER, textureFilter);
        GL45.glTextureParameteri(textureId, GL20.GL_TEXTURE_MAG_FILTER, textureFilter);

        GL45.glTextureStorage2D(textureId, 1, textureComponent.componentSpecs, image.getWidth(), image.getHeight());
        if (hasDefaultData) {
            GL45.glTextureSubImage2D(textureId, 0, 0, 0, image.getWidth(), image.getHeight(), textureComponent.component, textureComponent.componentType, image.getData());
            GL45.glGenerateTextureMipmap(textureId);
        }
    }

    public void bind(int unit) {
        if (lastBoundTextureId == textureId && lastBoundUnit == unit) return;
        GL45.glBindTextureUnit(unit, textureId);
        lastBoundTextureId = textureId;
        lastBoundUnit = unit;
    }

    public void unbind(int unit) {
        GL45.glBindTextureUnit(unit, 0);
        lastBoundTextureId = 0;
        lastBoundUnit = -1;
    }

    @Override
    public void dispose() throws PhotonException {
        System.out.println("Disposing texture " + textureId);
        GL45.glDeleteTextures(textureId);
        // If the deleted texture was considered the last bound one, reset tracking
        if (textureId == lastBoundTextureId) {
            lastBoundTextureId = 0;
            lastBoundUnit = -1;
            System.out.println("Reset lastBoundTextureId and lastBoundUnit after disposing texture");
        }
    }

    @Override
    public Texture getTexture() {
        return image;
    }

    public int getTextureId() {
        return textureId;
    }

    public enum GLTextureComponent {

        RGBA(GL20.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE),
        RGB(GL20.GL_RGB8, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE),
        DEPTH(GL20.GL_DEPTH_COMPONENT24, GL20.GL_DEPTH_COMPONENT, GL20.GL_UNSIGNED_INT),
        DEPTH_STENCIL(GL30.GL_DEPTH24_STENCIL8, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8);

        private final int componentSpecs;
        private final int component;
        private final int componentType;

        GLTextureComponent(int componentSpecs, int component, int componentType) {
            this.componentSpecs = componentSpecs;
            this.component = component;
            this.componentType = componentType;
        }
    }

}
