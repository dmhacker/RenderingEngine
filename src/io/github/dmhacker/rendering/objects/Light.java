package io.github.dmhacker.rendering.objects;

import java.awt.Color;

import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class Light {
	private Sphere sphere;
	private double kd;
	private double ks;
	private double sh;
	
	public static Light create(Color color, Vec3d position) {
		return new Light(color, position, 0.9, 1.0, 5000); 
	}
	
	private Light(Color color, Vec3d position, double diffuse, double specular, double specularHardness) {
		this.sphere = new Sphere(position, 1.0 / Math.log(specularHardness), Properties.create(color, Material.OPAQUE).setReflectivity(0));
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
	
	public Light setDiffusePower(double kd) {
		this.kd = kd;
		return this;
	}
	
	public Light setSpecularPower(double ks) {
		this.ks = ks;
		return this;
	}
	
	public Light getSpecularHardness(double sh) {
		this.sh = sh;
		return this;
	}
}
