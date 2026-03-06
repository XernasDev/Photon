package dev.xernas.photon.vulkan.pipeline;

import dev.xernas.photon.api.shader.IShader;
import dev.xernas.photon.api.shader.IUniform;
import dev.xernas.photon.api.shader.Shader;
import dev.xernas.photon.api.shader.ShaderModule;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.exceptions.PhotonException;
import dev.xernas.photon.utils.ShaderCompiler;
import dev.xernas.photon.utils.ShaderResource;
import dev.xernas.photon.utils.ShaderType;
import dev.xernas.photon.vulkan.device.VulkanDevice;

public class VulkanShader implements IShader {

    private final ShaderResource vertexResource;
    private final ShaderResource fragmentResource;
    private final VulkanDevice device;

    private VulkanShaderModule vertexShaderModule;
    private VulkanShaderModule fragmentShaderModule;

    public VulkanShader(Shader shader, VulkanDevice device) {
        this.vertexResource = shader.getVertexResource();
        this.fragmentResource = shader.getFragmentResource();
        this.device = device;
    }

    @Override
    public ShaderModule getVertexShaderModule() {
        return vertexShaderModule;
    }

    @Override
    public ShaderModule getFragmentShaderModule() {
        return fragmentShaderModule;
    }

    @Override
    public <T> IUniform<T> setUniform(String name, T value) {
        return null;
    }

    @Override
    public void changeShader(Shader shader) throws PhotonException {

    }

    @Override
    public void start() throws PhotonException {
        ShaderCompiler.SPIRV vertexSPIRV = ShaderCompiler.compileShaderCodeToSPIRV(vertexResource.filename(), vertexResource.shaderCode(), ShaderType.VERTEX);
        ShaderCompiler.SPIRV fragmentSPIRV = ShaderCompiler.compileShaderCodeToSPIRV(fragmentResource.filename(), fragmentResource.shaderCode(), ShaderType.FRAGMENT);

        // Create shader modules
        vertexShaderModule = new VulkanShaderModule(vertexSPIRV, device);
        fragmentShaderModule = new VulkanShaderModule(fragmentSPIRV, device);
        vertexShaderModule.start();
        fragmentShaderModule.start();
    }

    @Override
    public void dispose() throws PhotonException {
        vertexShaderModule.dispose();
        fragmentShaderModule.dispose();
    }
}
