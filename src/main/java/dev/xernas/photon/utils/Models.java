package dev.xernas.photon.utils;

import dev.xernas.photon.api.model.Model;

public class Models {

    public static Model createQuad(float size) {
        float halfSize = size / 2.0f;
        Model.Vertex[] vertices = {
                new Model.Vertex(-halfSize, -halfSize, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f),
                new Model.Vertex(halfSize, -halfSize, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
                new Model.Vertex(halfSize, halfSize, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f),
                new Model.Vertex(-halfSize, halfSize, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f)
        };
        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };
        return new Model("hydrogenQuad", vertices, indices);
    }
    public static Model createQuad() {
        return createQuad(1.0f);
    }

    public static Model createCube(float size) {
        float halfSize = size / 2.0f;
        Model.Vertex[] vertices = new Model.Vertex[]{
                new Model.Vertex(-halfSize, -halfSize, halfSize, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f), // Front face vert 1
                new Model.Vertex(halfSize, -halfSize, halfSize, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f), // Front face vert 2
                new Model.Vertex(halfSize, halfSize, halfSize, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f), // Front face vert 3
                new Model.Vertex(-halfSize, halfSize, halfSize, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f), // Front face vert 4

                new Model.Vertex(halfSize, -halfSize, -halfSize, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f), // Back face vert 1
                new Model.Vertex(-halfSize, -halfSize, -halfSize, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f), // Back face vert 2
                new Model.Vertex(-halfSize, halfSize, -halfSize, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f), // Back face vert 3
                new Model.Vertex(halfSize, halfSize, -halfSize, 1.0f, 1.0f, 0.0f, 0.0f, -1.0f), // Back face vert 4

                new Model.Vertex(halfSize, -halfSize, halfSize, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f), // Right face vert 1
                new Model.Vertex(halfSize, -halfSize, -halfSize, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f), // Right face vert 2
                new Model.Vertex(halfSize, halfSize, -halfSize, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f), // Right face vert 3
                new Model.Vertex(halfSize, halfSize, halfSize, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f), // Right face vert 4

                new Model.Vertex(-halfSize, -halfSize, -halfSize, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f), // Left face vert 1
                new Model.Vertex(-halfSize, -halfSize, halfSize, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f), // Left face vert 2
                new Model.Vertex(-halfSize, halfSize, halfSize, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f), // Left face vert 3
                new Model.Vertex(-halfSize, halfSize, -halfSize, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f), // Left face vert 4

                new Model.Vertex(-halfSize, halfSize, halfSize, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f), // Top face vert 1
                new Model.Vertex(halfSize, halfSize, halfSize, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f), // Top face vert 2
                new Model.Vertex(halfSize, halfSize, -halfSize, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f), // Top face vert 3
                new Model.Vertex(-halfSize, halfSize, -halfSize, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f), // Top face vert 4

                new Model.Vertex(-halfSize, -halfSize, -halfSize, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f), // Bottom face vert 1
                new Model.Vertex(halfSize, -halfSize, -halfSize, 1.0f, 1.0f, 0.0f, -1.0f, 0.0f), // Bottom face vert 2
                new Model.Vertex(halfSize, -halfSize, halfSize, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f), // Bottom face vert 3
                new Model.Vertex(-halfSize, -halfSize, halfSize, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f) // Bottom face vert 4
        };
        int[] indices = {
                0, 1, 2,      0, 2, 3,    // Front face
                4, 5, 6,      4, 6, 7,    // Back face
                8, 9, 10,     8, 10, 11,  // Right face
                12, 13, 14,   12, 14, 15, // Left face
                16, 17, 18,   16, 18, 19, // Top face
                20, 21, 22,   20, 22, 23  // Bottom face
        };
        return new Model("hydrogenCube", vertices, indices);
    }
    public static Model createCube() {
        return createCube(1.0f);
    }

}
