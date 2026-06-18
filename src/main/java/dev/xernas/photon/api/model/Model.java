package dev.xernas.photon.api.model;

import dev.xernas.photon.api.DrawMode;

import java.util.UUID;

public class Model {

    private final String modelTag;
    private final float[] vertices;
    private final int[] indices;
    private final float[] texCoords;
    private final float[] normals;
    private final ModelSettings settings;

    private boolean flipV = true;

    public Model(String modelTag, Vertex[] vertices, int[] indices, ModelSettings settings) {
        this.modelTag = modelTag;
        this.vertices = new float[vertices.length * 3];
        this.texCoords = new float[vertices.length * 2];
        this.normals = new float[vertices.length * 3];
        this.indices = indices;

        for (int i = 0; i < vertices.length; i++) {
            Vertex v = vertices[i];
            this.vertices[i * 3] = v.x;
            this.vertices[i * 3 + 1] = v.y;
            this.vertices[i * 3 + 2] = v.z;

            this.texCoords[i * 2] = v.u;
            this.texCoords[i * 2 + 1] = v.v;

            this.normals[i * 3] = v.normalX;
            this.normals[i * 3 + 1] = v.normalY;
            this.normals[i * 3 + 2] = v.normalZ;
        }

        this.settings = settings;
    }

    public Model(String modelTag, Vertex[] vertices, int[] indices) {
        this(modelTag, vertices, indices, ModelSettings.DEFAULT_SETTINGS);
    }

    public Model(Vertex[] vertices, int[] indices, ModelSettings settings) {
        this(UUID.randomUUID().toString(), vertices, indices, settings);
    }

    public Model(Vertex[] vertices, int[] indices) {
        this(UUID.randomUUID().toString(), vertices, indices, ModelSettings.DEFAULT_SETTINGS);
    }

    public Model(String modelTag, float[] vertices, int[] indices, float[] texCoords, float[] normals, ModelSettings settings) {
        this.modelTag = modelTag;
        this.vertices = vertices;
        this.indices = indices;
        this.texCoords = texCoords;
        this.normals = normals;
        this.settings = settings;
    }

    public Model(String modelTag, float[] vertices, int[] indices, float[] texCoords, float[] normals) {
        this(modelTag, vertices, indices, texCoords, normals, ModelSettings.DEFAULT_SETTINGS);
    }

    public Model(float[] vertices, int[] indices, float[] texCoords, float[] normals, ModelSettings settings) {
        this(UUID.randomUUID().toString(), vertices, indices, texCoords, normals, settings);
    }

    public Model(float[] vertices, int[] indices, float[] texCoords, float[] normals) {
        this(UUID.randomUUID().toString(), vertices, indices, texCoords, normals, ModelSettings.DEFAULT_SETTINGS);
    }

    public String getModelTag() {
        return modelTag;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getTexCoords() {
        if (texCoords == null) return null;
        if (!flipV) return texCoords;
        // Inverse V (y) : v -> 1 - v
        float[] flipped = new float[texCoords.length];
        for (int i = 0; i < texCoords.length; i += 2) {
            flipped[i] = texCoords[i]; // u
            if (i + 1 < texCoords.length) flipped[i + 1] = 1.0f - texCoords[i + 1]; // v
        }
        return flipped;
    }

    public float[] getNormals() {
        return normals;
    }

    public ModelSettings getSettings() {
        return settings;
    }

    public Model flipV() {
        flipV = !flipV;
        return this;
    }

    public record Vertex(float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ) {

    }

    public record ModelSettings(DrawMode drawMode, boolean usePerspective, boolean backfaceCulling) {

        public static final ModelSettings DEFAULT_SETTINGS = new ModelSettings(DrawMode.TRIANGLES, true, true);
        public static final ModelSettings ORTHO_SETTINGS = new ModelSettings(DrawMode.TRIANGLES, false, true);
        public static final ModelSettings FULL_PERSPECTIVE_SETTINGS = new ModelSettings(DrawMode.TRIANGLES, true, false);

    }

}
