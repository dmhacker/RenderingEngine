package io.github.dmhacker.rendering.objects;

import java.awt.Color;

import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class Light {
	private Sphere sphere;
	
	public Light(Vec3d position, double radius) {
		this.sphere = new Sphere(position, radius, new Properties());
	}
	
	public Color getColor() {
		return sphere.getProperties().getColor();
	}
	
	public Vec3d getPosition() {
		return sphere.getCenter();
	}
	
	public double getIntersection(Ray ray) {
		return sphere.getIntersection(ray);
	}
}
