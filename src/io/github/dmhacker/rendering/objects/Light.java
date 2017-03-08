package io.github.dmhacker.rendering.objects;

import java.awt.Color;

import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class Light {
	private static final double DEFAULT_DIFFUSE_CONSTANT = 0.9;
	private static final double DEFAULT_SPECULAR_CONSTANT = 1.0;
	private static final double DEFAULT_SPECULAR_HARDNESS = 5000;
	
	private Object3d shape;
	private Vec3d position;
	private double radius;
	private double kd;
	private double ks;
	private double sh;
	
	public static Light create(Color color, Vec3d position) {
		Sphere sphere = new Sphere(position, 0.1, Properties.create(color, Material.OPAQUE).setReflectivity(0));
		return Light.create(color, position, sphere);
	}
	
	public static Light create(Color color, Vec3d position, Object3d shape) {
		return new Light(color, position, shape, DEFAULT_DIFFUSE_CONSTANT, DEFAULT_SPECULAR_CONSTANT, DEFAULT_SPECULAR_HARDNESS); 
	}
	
	private Light(Color color, Vec3d position, Object3d shape, double diffuse, double specular, double specularHardness) {
		this.shape = shape;
		this.position = position;
		this.kd = diffuse;
		this.ks = specular;
		this.sh = specularHardness;
		
		// Set shape center to position
		shape.translate(shape.getCenter().negative());
		shape.translate(position);
		
		this.radius = 0.0;
		for (Vec3d vertex : shape.getBoundingVertices()) {
			double dist = vertex.subtract(position).distance();
			if (dist > radius) {
				this.radius = dist;
			}
		}
	}
	
	public Color getColor() {
		return shape.getProperties().getColor();
	}
	
	public Vec3d getPosition() {
		return position;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public double getIntersection(Ray ray) {
		return shape.getIntersection(ray);
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
