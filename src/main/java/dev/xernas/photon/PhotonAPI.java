package dev.xernas.photon;

import dev.xernas.photon.api.framebuffer.IFramebuffer;
import dev.xernas.photon.api.model.IMesh;
import dev.xernas.photon.api.IRenderer;
import dev.xernas.photon.api.shader.IShader;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.api.texture.Texture;
import dev.xernas.photon.exceptions.PhotonException;
import dev.xernas.photon.api.window.Window;

public class PhotonAPI {

    public static final String NAME = "Photon";
    public static final String VERSION = "2.0.0";

    private static Library library;
    private static boolean initialized = false;
    private static String appName;
    private static String appVersion;
    private static boolean debug;

    public static void init(Library lib, String appName, String appVersion, boolean debug) {
        PhotonAPI.library = lib;
        PhotonAPI.appName = appName;
        PhotonAPI.appVersion = appVersion;
        PhotonAPI.debug = debug;
        PhotonAPI.initialized = true;
    }

    public static Library getLibrary() {
        return library;
    }

    public static String getAppName() {
        return appName;
    }

    public static String getAppVersion() {
        return appVersion;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static IRenderer<IFramebuffer, IShader, IMesh, ITexture> getRenderer(Window window, boolean vsync) {
        if (!initialized) throw new IllegalStateException("PhotonAPI is not initialized. Call PhotonAPI.init() first.");
        return (IRenderer<IFramebuffer, IShader, IMesh, ITexture>) library.createRenderer(window, vsync, debug);
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
