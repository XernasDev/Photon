package dev.xernas.photon.vulkan;

import dev.xernas.photon.PhotonAPI;
import dev.xernas.photon.api.*;
import dev.xernas.photon.api.framebuffer.Framebuffer;
import dev.xernas.photon.api.framebuffer.IFramebuffer;
import dev.xernas.photon.api.model.IMesh;
import dev.xernas.photon.api.model.Model;
import dev.xernas.photon.api.shader.IUniform;
import dev.xernas.photon.api.shader.Shader;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.api.texture.Texture;
import dev.xernas.photon.exceptions.PhotonException;
import dev.xernas.photon.vulkan.device.VulkanDevice;
import dev.xernas.photon.vulkan.device.VulkanPhysicalDevice;
import dev.xernas.photon.vulkan.pipeline.VulkanPipeline;
import dev.xernas.photon.vulkan.pipeline.VulkanShader;
import dev.xernas.photon.vulkan.swapchain.VulkanFramebuffers;
import dev.xernas.photon.vulkan.swapchain.VulkanRenderPass;
import dev.xernas.photon.vulkan.swapchain.VulkanSwapChain;
import dev.xernas.photon.api.window.Window;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;

public class VulkanRenderer implements IRenderer<IFramebuffer, VulkanShader, IMesh, ITexture> {

    private final Window window;

    private final VulkanInstance instance;
    private final VulkanSurface surface;
    private final VulkanPhysicalDevice physicalDevice;
    private final VulkanDevice device;
    private final VulkanSwapChain swapChain;
    private final VulkanRenderPass renderPass;
    private final VulkanPipeline pipeline;
    private final VulkanFramebuffers framebuffers;
    private final VulkanCommandPool commandPool;
    private final VulkanSynchronisation synchronisation;

    public VulkanRenderer(Window window, boolean vsync, boolean enableValidation) {
        this.window = window;
        this.instance = new VulkanInstance(enableValidation);
        this.surface = new VulkanSurface(window, instance);
        this.physicalDevice = new VulkanPhysicalDevice(instance, surface);
        this.device = new VulkanDevice(physicalDevice);
        this.swapChain = new VulkanSwapChain(vsync, window, device, physicalDevice, surface);
        this.renderPass = new VulkanRenderPass(swapChain, device);
        this.pipeline = new VulkanPipeline(new VulkanShader(null, device), renderPass, swapChain, device);
        this.framebuffers = new VulkanFramebuffers(swapChain, renderPass, device);
        this.commandPool = new VulkanCommandPool(framebuffers, swapChain, device);
        this.synchronisation = new VulkanSynchronisation(device);
    }

    @Override
    public void render(IFramebuffer framebuffer, VulkanShader shader, IMesh mesh, Runnable operations) throws PhotonException {

    }

    @Override
    public void swapBuffers() throws PhotonException {
        if (window.framebufferResized()) {
            recreateSwapChain();
            window.setFramebufferResized(false);
            return;
        }
        int imageIndex = swapChain.acquireNextImage(synchronisation);
        if (imageIndex == -1) {
            recreateSwapChain();
            return;
        }
        VulkanCommandBuffer commandBuffer = commandPool.getCommandBuffer(imageIndex);
        commandPool.recordCommandBuffer(imageIndex, commandBuffer, pipeline, renderPass);
        swapChain.submitCommandBuffer(imageIndex, synchronisation, commandBuffer);
        int err = swapChain.presentImage(imageIndex, synchronisation, this);
        if (err == -1) recreateSwapChain();
    }

    @Override
    public void clear(Color color) throws PhotonException {

    }

    @Override
    public boolean useTexture(String name, ITexture texture, int slot, VulkanShader shader) {
        return false;
    }

    @Override
    public void empty() {

    }

    @Override
    public ITexture loadTexture(Texture texture) throws PhotonException {
        return null;
    }

    @Override
    public IMesh loadMesh(Model model) throws PhotonException {
        return null;
    }

    @Override
    public VulkanShader loadShader(Shader shader) throws PhotonException {
        return null;
    }

    @Override
    public IFramebuffer loadFramebuffer(Framebuffer framebuffer) throws PhotonException {
        return null;
    }

    @Override
    public boolean unloadTexture(ITexture texture) throws PhotonException {
        return false;
    }

    @Override
    public boolean unloadMesh(IMesh mesh) throws PhotonException {
        return false;
    }

    @Override
    public boolean unloadShader(VulkanShader shader) throws PhotonException {
        return false;
    }

    @Override
    public boolean unloadFramebuffer(IFramebuffer framebuffer) throws PhotonException {
        return false;
    }

    public void recreateSwapChain() throws PhotonException {
        device.waitIdle();
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(window.getHandle(), width, height);

        // Wait until window is not minimized (size != 0)
        while (width[0] == 0 || height[0] == 0) {
            GLFW.glfwGetFramebufferSize(window.getHandle(), width, height);
            GLFW.glfwWaitEvents();
        }
        framebuffers.dispose();
        renderPass.dispose();
        pipeline.dispose();
        swapChain.dispose();

        swapChain.start();
        renderPass.start();
        pipeline.start();
        framebuffers.start();
    }

    @Override
    public void start() throws PhotonException {
        if (!PhotonAPI.isInitialized()) throw new PhotonException("PhotonAPI not initialized");
        instance.start();
        surface.start();
        physicalDevice.start();
        device.start();
        swapChain.start();
        renderPass.start();
        pipeline.start();
        framebuffers.start();
        commandPool.start();
        synchronisation.start();
    }

    @Override
    public void dispose() throws PhotonException {
        synchronisation.dispose();
        commandPool.dispose();
        framebuffers.dispose();
        pipeline.dispose();
        renderPass.dispose();
        swapChain.dispose();
        device.dispose();
        physicalDevice.dispose();
        surface.dispose();
        instance.dispose();
    }

}
