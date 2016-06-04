package io.github.dmhacker.rendering.objects;

import java.util.ArrayList;
import java.util.List;

import io.github.dmhacker.rendering.kdtrees.BoundingBox;
import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class Sphere implements Object3d {
	private Vec3d center;
	private double radius;
	private Properties properties;
	private List<Vec3d> boundingVertices;
	private BoundingBox bbox;
	
	public Sphere(Vec3d vec, double radius, Properties properties) {
		this.center = vec;
		this.radius = radius;
		this.properties = properties;
		
		this.boundingVertices = new ArrayList<Vec3d>();
		boundingVertices.add(vec.add(new Vec3d(-radius, 0, 0)));
		boundingVertices.add(vec.add(new Vec3d(radius, 0, 0)));
		boundingVertices.add(vec.add(new Vec3d(0, -radius, 0)));
		boundingVertices.add(vec.add(new Vec3d(0, radius, 0)));
		boundingVertices.add(vec.add(new Vec3d(0, 0, -radius)));
		boundingVertices.add(vec.add(new Vec3d(0, 0, radius)));
		
		this.bbox = new BoundingBox(boundingVertices);
	}
	
	public Sphere(double x, double y, double z, double radius, Properties properties) {
		this(new Vec3d(x, y, z), radius, properties);
	}
	
	public Vec3d getCenter() {
		return center;
	}
	
	public double getRadius() {
		return radius;
	}
	
	@Override
	public double getIntersection(Ray ray) {
		Vec3d originMinusCenter = ray.getOrigin().subtract(getCenter());
		double dot = ray.getDirection().dotProduct(originMinusCenter);
		double discriminant = Math.pow(dot, 2) - originMinusCenter.distanceSquared() + radius * radius;
		if (discriminant < 0)
			return -1;
		return -dot - Math.sqrt(discriminant);
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public List<Vec3d> getBoundingVertices() {
		return boundingVertices;
	}
	
	public boolean equals(Object o) {
		if (o != null && o instanceof Sphere) {
			Sphere s = (Sphere) o;
			return radius == s.radius && center.equals(s.center);
		}
		return false;
	}
	
	public String toString() {
		return "Sphere["+getCenter()+", "+radius+"]";
	}

	@Override
	public BoundingBox getBoundingBox() {
		return bbox;
	}
}
