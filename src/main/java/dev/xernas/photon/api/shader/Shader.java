package dev.xernas.photon.api.shader;

import dev.xernas.photon.utils.ShaderResource;

public class Shader {

    private final String name;
    private final ShaderResource vertexResource;
    private final ShaderResource fragmentResource;

    public Shader(String name, ShaderResource vertexResource, ShaderResource fragmentResource) {
        this.name = name;
        this.vertexResource = vertexResource;
        this.fragmentResource = fragmentResource;
    }

    public String getName() {
        return name;
    }

    public ShaderResource getVertexResource() {
        return vertexResource;
    }

    public ShaderResource getFragmentResource() {
        return fragmentResource;
    }

}
