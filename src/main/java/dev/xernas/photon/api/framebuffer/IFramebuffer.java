package dev.xernas.photon.api.framebuffer;

import dev.xernas.photon.api.PhotonLogic;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.exceptions.PhotonException;

import java.awt.*;

public interface IFramebuffer extends PhotonLogic {

    void clear(Color color);

    default void clear() {
        clear(Color.BLACK);
    }

    void resize(int width, int height) throws PhotonException;

    ITexture getAttachmentTexture(FramebufferAttachment attachment);

    int getWidth();
    int getHeight();

}
