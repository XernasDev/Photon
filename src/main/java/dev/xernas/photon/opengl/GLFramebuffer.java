package dev.xernas.photon.opengl;

import dev.xernas.photon.api.framebuffer.FramebufferAttachment;
import dev.xernas.photon.api.framebuffer.IFramebuffer;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.exceptions.GLException;
import dev.xernas.photon.exceptions.PhotonException;
import org.lwjgl.opengl.GL45;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GLFramebuffer implements IFramebuffer {

    private final List<FramebufferAttachment> attachments = new ArrayList<>();

    private final Map<FramebufferAttachment, GLTexture> attachedTextures = new HashMap<>();
    private final List<GLRenderbuffer> attachedRenderbuffers = new ArrayList<>();

    private int width;
    private int height;

    private int framebufferId = 0;

    public GLFramebuffer(int width, int height, List<FramebufferAttachment> attachments) {
        this.width = width;
        this.height = height;
        this.attachments.addAll(attachments);
    }

    @Override
    public void start() throws PhotonException {
        if (width <= 0 || height <= 0) return;
        framebufferId = GL45.glCreateFramebuffers();
        for (FramebufferAttachment attachment : attachments) {
            if (attachment.isTexture()) {
                GLTexture.GLTextureComponent componentType = switch (attachment) {
                    case COLOR_TEXTURE -> GLTexture.GLTextureComponent.RGBA;
                    case DEPTH_TEXTURE -> GLTexture.GLTextureComponent.DEPTH;
                    case DEPTH_STENCIL_TEXTURE -> GLTexture.GLTextureComponent.DEPTH_STENCIL;
                    default -> throw new GLException("Invalid texture attachment type: " + attachment);
                };
                GLTexture textureAttachment = new GLTexture(width, height, componentType);
                textureAttachment.start();
                attach(attachment, textureAttachment);
            }
            else {
                GLRenderbuffer renderbufferAttachment = new GLRenderbuffer(width, height);
                renderbufferAttachment.start();
                attach(attachment, renderbufferAttachment);
            }
        }

        int status = GL45.glCheckNamedFramebufferStatus(framebufferId, GL45.GL_FRAMEBUFFER);

        if (status != GL45.GL_FRAMEBUFFER_COMPLETE) throw new GLException("Failed to create framebuffer: " + GLUtils.getError(status));
        bind();
    }

    @Override
    public void dispose() throws PhotonException {
        if (width <= 0 || height <= 0) return;
        for (GLTexture texture : attachedTextures.values()) texture.dispose();
        for (GLRenderbuffer renderbuffer : attachedRenderbuffers) renderbuffer.dispose();
        attachedTextures.clear();
        attachedRenderbuffers.clear();
        if (GL45.glGetInteger(GL45.GL_FRAMEBUFFER_BINDING) == framebufferId) GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, 0);
        GL45.glDeleteFramebuffers(framebufferId);
    }

    public void bind() {
        if (GL45.glGetInteger(GL45.GL_FRAMEBUFFER_BINDING) == framebufferId) return;
        GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, framebufferId);
    }

    public static void bindDefault() {
        if (GL45.glGetInteger(GL45.GL_FRAMEBUFFER_BINDING) == 0) return;
        GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, 0);
    }

    public void attach(FramebufferAttachment attachment, GLTexture texture) {
        switch (attachment) {
            case COLOR_TEXTURE -> {
                GL45.glNamedFramebufferTexture(framebufferId, GL45.GL_COLOR_ATTACHMENT0, texture.getTextureId(), 0);
                GL45.glNamedFramebufferDrawBuffers(framebufferId, GL45.GL_COLOR_ATTACHMENT0);
            }
            case DEPTH_TEXTURE -> GL45.glNamedFramebufferTexture(framebufferId, GL45.GL_DEPTH_ATTACHMENT, texture.getTextureId(), 0);
            case DEPTH_STENCIL_TEXTURE -> GL45.glNamedFramebufferTexture(framebufferId, GL45.GL_DEPTH_STENCIL_ATTACHMENT, texture.getTextureId(), 0);
        }
        attachedTextures.put(attachment, texture);
    }

    public void attach(FramebufferAttachment attachment, GLRenderbuffer renderbuffer) {
        switch (attachment) {
            case DEPTH_RENDERBUFFER -> GL45.glNamedFramebufferRenderbuffer(framebufferId, GL45.GL_DEPTH_ATTACHMENT, GL45.GL_RENDERBUFFER, renderbuffer.getRenderbufferId());
            case STENCIL_RENDERBUFFER -> GL45.glNamedFramebufferRenderbuffer(framebufferId, GL45.GL_STENCIL_ATTACHMENT, GL45.GL_RENDERBUFFER, renderbuffer.getRenderbufferId());
            case DEPTH_STENCIL_RENDERBUFFER -> GL45.glNamedFramebufferRenderbuffer(framebufferId, GL45.GL_DEPTH_STENCIL_ATTACHMENT, GL45.GL_RENDERBUFFER, renderbuffer.getRenderbufferId());
        }
        attachedRenderbuffers.add(renderbuffer);
    }

    @Override
    public void clear(Color color) {
        GLUtils.clear(this, color);
    }

    @Override
    public void resize(int width, int height) throws PhotonException {
        boolean hadFramebuffer = framebufferId != 0;
        if (hadFramebuffer) dispose();
        this.width = width;
        this.height = height;
        if (hadFramebuffer) start();
    }

    @Override
    public ITexture getAttachmentTexture(FramebufferAttachment attachment) {
        return attachedTextures.get(attachment);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public int getFramebufferId() {
        return framebufferId;
    }
}
