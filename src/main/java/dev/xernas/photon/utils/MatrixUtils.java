package dev.xernas.photon.utils;

import dev.xernas.photon.api.Transform;
import dev.xernas.photon.api.window.Window;
import dev.xernas.photon.exceptions.PhotonException;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MatrixUtils {

    public static Matrix4f createTransformationMatrix(Transform transform) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.identity().translate(transform.getPosition())
                .rotateX((float) Math.toRadians(transform.getRotation().x))
                .rotateY((float) Math.toRadians(transform.getRotation().y))
                .rotateZ((float) Math.toRadians(transform.getRotation().z))
                .scale(transform.getScale());
        return matrix4f;
    }

    public static Matrix4f createViewMatrix(Transform.CameraTransform transform) {
        Vector3f position = transform.getPosition();
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();
        Vector3f rotation = transform.getRotation();
        viewMatrix.rotate((float) Math.toRadians(rotation.x), Direction.RIGHT)
                .rotate((float) Math.toRadians(rotation.y), Direction.UP)
                .rotate((float) Math.toRadians(rotation.z), Direction.FORWARD);
        viewMatrix.translate(-position.x, -position.y, -position.z);
        return viewMatrix;
    }

    public static Matrix4f create2DViewMatrix(Transform.CameraTransform transform) {
        Vector3f position = transform.getPosition();
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();
        viewMatrix.translate(-position.x, -position.y, 0);
        return viewMatrix;
    }

    public static Matrix4f createProjectionMatrix(Window window, int fov, float zNear, float zFar) {
        return new Matrix4f().identity()
                .setPerspective(
                        (float) Math.toRadians(fov),
                        (float) window.getWidth() / window.getHeight(),
                        zNear,
                        zFar
                );
    }

    public static Matrix3f createNormalMatrix(Matrix4f modelMatrix) {
        return new Matrix3f(modelMatrix).invert().transpose();
    }

    public static Matrix4f createOrthoMatrix(Window window) {
        float aspectRatio = (float) window.getWidth() / window.getHeight();
        float scale = 1.0f; // Adjust this if you want to zoom in/out
        float left, right, bottom, top;
        if (aspectRatio >= 1.0f) {
            // Wider than tall
            left = -scale * aspectRatio;
            right = scale * aspectRatio;
            bottom = -scale;
            top = scale;
        } else {
            // Taller than wide
            left = -scale;
            right = scale;
            bottom = -scale / aspectRatio;
            top = scale / aspectRatio;
        }

        return new Matrix4f().identity().ortho(left, right, bottom, top, -1.0f, 100.0f);
    }

}
