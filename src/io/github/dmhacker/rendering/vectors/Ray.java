package io.github.dmhacker.rendering.vectors;

public class Ray {
	private Vec3d origin;
	private Vec3d direction;
	private Vec3d inverseDirection;
	
	public Ray(Vec3d origin, Vec3d direction) {
		this.origin = origin;
		this.direction = direction;
		this.inverseDirection = new Vec3d(1.0 / direction.getX(), 1.0 / direction.getY(), 1.0 / direction.getZ());
	}
	
	public Vec3d getOrigin() {
		return origin;
	}
	
	public Vec3d getDirection() {
		return direction;
	}
	
	public Vec3d getInverseDirection() {
		return inverseDirection;
	}
	
	public Vec3d evaluate(double t) {
		return origin.add(direction.multiply(t));
	}
	
	public String toString() {
		return "["+origin+", "+direction+"]";
	}
	
	public static Ray between(Vec3d origin, Vec3d passThrough) {
		return new Ray(origin, passThrough.subtract(origin).normalize());
	}
}
