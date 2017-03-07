package io.github.dmhacker.rendering.objects;

import java.util.ArrayList;
import java.util.List;

import io.github.dmhacker.rendering.objects.meshes.GenericMesh;
import io.github.dmhacker.rendering.objects.meshes.Mesh;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class Scene {
	private Vec3d camera;
	private Vec3d rotationAxis;
	private double rotateAngle;
	private double cameraSize;
	private List<Object3d> objects;
	private List<Light> lights;
	private double zoom;
	
	public Scene(Vec3d camera) {
		this(camera, new Vec3d(0, 0, 1), 0);
	}
	
	public Scene(Vec3d camera, Vec3d rotationAxis, double rotateAngle) {
		this(camera, rotationAxis, rotateAngle, 1, 1);
	}
	
	public Scene(Vec3d camera, Vec3d rotationAxis, double rotateAngle, double cameraSize, double zoom) {
		this.camera = camera;
		this.rotationAxis = rotationAxis;
		this.rotateAngle = rotateAngle;
		this.cameraSize = cameraSize;
		this.objects = new ArrayList<>();
		this.lights = new ArrayList<>();
		this.zoom = zoom;
	}
	
	public double getZoom() {
		return zoom;
	}
	
	public void add(Mesh mesh) {
		objects.addAll(mesh.getFacets());
	}
	
	public void add(Object3d obj) {
		objects.add(obj);
	}
	
	public void add(Light light) {
		lights.add(light);
	}
	
	public Vec3d getCamera() {
		return camera;
	}
	
	public Vec3d getCameraRotationAxis() {
		return rotationAxis;
	}
	
	public double getCameraRotationAngle() {
		return rotateAngle;
	}
	
	public double getCameraSize() {
		return cameraSize;
	}

	public List<Object3d> getObjects() {
		return objects;
	}
	
	public List<Light> getLights() {
		return lights;
	}
	
	public void addFloor(double surfaceY, Properties floorProperties) {
		Vec3d topLeft = new Vec3d(-1e5, surfaceY, 1e5);
		Vec3d bottomLeft = new Vec3d(-1e5, surfaceY, -1e5);
		Vec3d topRight = new Vec3d(1e5, surfaceY, 1e5);
		Vec3d bottomRight = new Vec3d(1e5, surfaceY, -1e5);

		Triangle topLeftPortion = new Triangle(null, bottomLeft, topLeft, topRight, floorProperties);
		Triangle bottomRightPortion = new Triangle(null, bottomLeft, bottomRight, topRight, floorProperties);
		
		List<Triangle> floorTriangles = new ArrayList<Triangle>();
		floorTriangles.add(topLeftPortion);
		floorTriangles.add(bottomRightPortion);
		GenericMesh floorMesh = new GenericMesh(floorTriangles);
		
		add(floorMesh);
	}
}
