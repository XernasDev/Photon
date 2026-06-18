package dev.xernas.photon.api;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Transform {

    private final Vector3f defaultPos;
    private final Vector3f defaultRot;
    private final Vector3f position;
    private final Vector3f rotation;
    private final Vector3f scale;

    private final List<Transform> linked = new ArrayList<>();

    public Transform() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
        this.defaultPos = new Vector3f(0, 0, 0);
        this.defaultRot = new Vector3f(0, 0, 0);
    }

    public Transform(Vector3f position) {
        this.position = position;
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);;
        this.defaultPos = position;
        this.defaultRot = new Vector3f(0, 0, 0);
    }

    public Transform(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
        this.scale = new Vector3f(1, 1, 1);;
        this.defaultPos = position;
        this.defaultRot = rotation;
    }

    public Transform(Vector3f position, Vector3f rotation, Vector3f scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.defaultPos = position;
        this.defaultRot = rotation;
    }

    public void link(Transform other) {
        this.linked.add(other);
        other.defaultPos.set(new Vector3f(defaultPos).add(other.defaultPos));
        other.defaultRot.set(new Vector3f(defaultRot).add(other.defaultRot));
        other.position.set(new Vector3f(position).add(other.position));
        other.rotation.set(new Vector3f(rotation).add(other.rotation));
        other.scale.set(new Vector3f(scale).mul(other.scale));
    }

    public void move(Vector3f position) {
        this.position.add(position);
        for (Transform other : linked) other.position.add(position);
    }

    public void incPosition(float x, float y, float z) {
        this.position.add(x, y, z);
        for (Transform other : linked) other.position.add(x, y, z);
    }

    public void rotate(Vector3f rotation) {
        this.rotation.add(rotation);
        for (Transform other : linked) other.rotation.add(rotation);
    }

    public void incRotation(float x, float y, float z) {
        this.rotation.add(x, y, z);
        for (Transform other : linked) other.rotation.add(x, y, z);
    }

    public Transform scale(Vector3f scale) {
        return scale(scale.x, scale.y, scale.z);
    }

    public Transform scale(float x, float y, float z) {
        this.scale.mul(x, y, z);
        for (Transform other : linked) other.scale.mul(x, y, z);
        return this;
    }

    public Transform scale(float scale) {
        this.scale.mul(scale);
        for (Transform other : linked) other.scale.mul(scale);
        return this;
    }

    public Transform setScale(float x, float y, float z) {
        setScale(new Vector3f(x, y, z));
        return this;
    }

    public Transform setScale(float scale) {
        return setScale(scale, scale, scale);
    }

    public Transform setScaleX(float x) {
        this.scale.x = x;
        return this;
    }

    public Transform setScaleY(float y) {
        this.scale.y = y;
        return this;
    }

    public Transform setScaleZ(float z) {
        this.scale.z = z;
        return this;
    }

    public void setPosition(float x, float y, float z) {
        setPosition(new Vector3f(x, y, z));
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public void setRotation(Vector3f rotation) {
        this.rotation.set(rotation);
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.set(new Vector3f(x, y, z));
    }

    public void setScale(Vector3f scale) {
        this.scale.set(scale);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getOffsetPosition(Vector3f offset) {
        return new Vector3f(defaultPos).add(offset);
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Vector3f getOffsetRotation(Vector3f offset) {
        return new Vector3f(defaultRot).add(offset);
    }

    public Vector3f getScale() {
        return scale;
    }



    public static class CameraTransform extends Transform {

        public CameraTransform() {
            super();
        }

        public CameraTransform(Vector3f position) {
            super(position);
        }

        public CameraTransform(Vector3f position, Vector3f rotation) {
            super(position, rotation);
        }

        @Override
        public void move(Vector3f offset) {
            Vector3f pos = getPosition();
            Vector3f rot = getRotation();
            if (offset.z != 0) {
                pos.x += (float) Math.sin(Math.toRadians(rot.y)) * -1.0f * offset.z;
                pos.z += (float) Math.cos(Math.toRadians(rot.y)) * offset.z;
            }
            if (offset.x != 0) {
                pos.x += (float) Math.sin(Math.toRadians(rot.y - 90)) * -1.0f * offset.x;
                pos.z += (float) Math.cos(Math.toRadians(rot.y - 90)) * offset.x;
            }
            pos.y += offset.y;
        }

        public Vector3f getForwardVector() {
            Vector3f forward = new Vector3f();
            Vector3f rot = getRotation();
            forward.x = (float) (Math.sin(Math.toRadians(rot.y)) * Math.cos(Math.toRadians(rot.x)));
            forward.y = (float) Math.sin(Math.toRadians(-rot.x));
            forward.z = (float) (Math.cos(Math.toRadians(rot.y)) * Math.cos(Math.toRadians(rot.x)));
            forward.normalize();
            return forward;
        }
    }

}
