package io.github.dmhacker.rendering.objects;

import java.awt.Color;

import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class Light {
	private Sphere sphere;
	private double kd;
	private double ks;
	private double sh;
	
	public Light(Vec3d position, double radius, double diffuse, double specular, double specularHardness) {
		this.sphere = new Sphere(position, radius, new Properties());
		this.kd = diffuse;
		this.ks = specular;
		this.sh = specularHardness;
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
	
	public double getDiffusePower() {
		return kd;
	}
	
	public double getSpecularPower() {
		return ks;
	}
	
	public double getSpecularHardness() {
		return sh;
	}
}
