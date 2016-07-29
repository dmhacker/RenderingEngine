package io.github.dmhacker.rendering.objects;

import java.awt.Color;

import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class Light {
	private Sphere sphere;
	private double kd;
	private double ks;
	private double sh;
	
	public Light(Color color, Vec3d position, double diffuse, double specular, double specularHardness) {
		this.sphere = new Sphere(position, 1.0 / Math.log(specularHardness), new Properties(color, Material.OPAQUE, 0, 1));
		this.kd = diffuse;
		this.ks = specular;
		this.sh = specularHardness;
	}
	
	public Color getColor() {
		return sphere.getProperties().getColor();
	}
	
	public double getRadius() {
		return sphere.getRadius();
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
