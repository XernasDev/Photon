package dev.xernas.photon.opengl;

import dev.xernas.photon.api.shader.IShader;
import dev.xernas.photon.api.shader.Shader;
import dev.xernas.photon.api.shader.ShaderModule;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.exceptions.GLException;
import dev.xernas.photon.exceptions.PhotonException;
import dev.xernas.photon.utils.GlobalUtilitaries;
import dev.xernas.photon.utils.ShaderType;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class GLShader implements IShader {

    private static int lastBoundProgramId = 0;

    private final Map<String, Integer> uniforms = new HashMap<>();

    private GLShaderModule vertexShader;
    private GLShaderModule fragmentShader;

    private int programId;

    public GLShader(Shader shader) {
        this.vertexShader = new GLShaderModule(shader.getVertexResource(), ShaderType.VERTEX);
        this.fragmentShader = new GLShaderModule(shader.getFragmentResource(), ShaderType.FRAGMENT);
    }

    @Override
    public ShaderModule getVertexShaderModule() {
        return vertexShader;
    }

    @Override
    public ShaderModule getFragmentShaderModule() {
        return fragmentShader;
    }

    @Override
    public <T> GLUniform<T> setUniform(String name, T value) {
        int location = uniforms.getOrDefault(name, -1);
        if (location == -1) return null;
        GLUniform<T> uniform =  new GLUniform<>(name, location);
        uniform.set(value);
        return uniform;
    }

    @Override
    public void changeShader(Shader shader) throws PhotonException {
        GLShaderModule oldVertexShader = this.vertexShader;
        GLShaderModule oldFragmentShader = this.fragmentShader;
        try {
            dispose();
            this.vertexShader = new GLShaderModule(shader.getVertexResource(), ShaderType.VERTEX);
            this.fragmentShader = new GLShaderModule(shader.getFragmentResource(), ShaderType.FRAGMENT);
            start();
        } catch (PhotonException e) {
            dispose();
            this.vertexShader = oldVertexShader;
            this.fragmentShader = oldFragmentShader;
            start();
            throw e;
        }
    }

    @Override
    public void start() throws PhotonException {
        vertexShader.start();
        fragmentShader.start();

        programId = getProgram();
        uniforms.putAll(getUniformLocations());
    }

    public void bind() {
        if (lastBoundProgramId == programId) return;
        GL45.glUseProgram(programId);
        lastBoundProgramId = programId;
    }

    public void unbind() {
        GL45.glUseProgram(0);
        lastBoundProgramId = 0;
    }

    private int getProgram() throws PhotonException {
        int program = GlobalUtilitaries.requireNotEquals(GL45.glCreateProgram(), 0, "Error creating shader program");
        if (program == 0) throw new GLException("Could not create program");
        GL45.glAttachShader(program, vertexShader.getShaderId());
        GL45.glAttachShader(program, fragmentShader.getShaderId());
        GL45.glLinkProgram(program);
        if (GL45.glGetProgrami(program, GL45.GL_LINK_STATUS) == GL20.GL_FALSE) throw new GLException("Could not link shader program");

        if (vertexShader.getShaderId() != 0) {
            GL45.glDetachShader(program, vertexShader.getShaderId());
            vertexShader.dispose();
        }
        if (fragmentShader.getShaderId() != 0) {
            GL45.glDetachShader(program, fragmentShader.getShaderId());
            fragmentShader.dispose();
        }

        GL45.glValidateProgram(program);
        if (GL45.glGetProgrami(program, GL45.GL_VALIDATE_STATUS) == GL20.GL_FALSE) throw new GLException("Could not validate shader program");
        return program;
    }

    private Map<String, Integer> getUniformLocations() {
        Map<String, Integer> uniformLocations = new HashMap<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer count = stack.mallocInt(1);
            GL45.glGetProgramiv(programId, GL20.GL_ACTIVE_UNIFORMS, count);
            for (int i = 0; i < count.get(0); i++) {

                IntBuffer size = stack.mallocInt(1);
                IntBuffer type = stack.mallocInt(1);
                String name = GL45.glGetActiveUniform(programId, i, size, type).trim();
                if (name.endsWith("[0]")) name = name.substring(0, name.length() - 3);
                if (size.get(0) > 1) {
                    for (int j = 0; j < size.get(0); j++) {
                        String arrayName = name + "[" + j + "]";
                        int location = GL45.glGetUniformLocation(programId, arrayName);
                        uniformLocations.put(arrayName, location);
                    }
                } else {
                    int location = GL45.glGetUniformLocation(programId, name);
                    uniformLocations.put(name, location);
                }
            }
        }
        return uniformLocations;
    }

    @Override
    public void dispose() throws PhotonException {
        uniforms.clear();
        if (programId == 0) return;
        GL45.glDeleteProgram(programId);
        programId = 0;
    }

    public int getProgramId() {
        return programId;
    }
}
