package dev.xernas.photon.api.shader;

import dev.xernas.photon.api.PhotonLogic;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.exceptions.PhotonException;

public interface IShader extends PhotonLogic {

    ShaderModule getVertexShaderModule();
    ShaderModule getFragmentShaderModule();

    <T> IUniform<T> setUniform(String name, T value);

    void changeShader(Shader shader) throws PhotonException;

}
