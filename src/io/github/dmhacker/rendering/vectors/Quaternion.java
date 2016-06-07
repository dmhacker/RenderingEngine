package io.github.dmhacker.rendering.vectors;

public class Quaternion {
	private double w;
	private double x;
	private double y;
	private double z;
	private Vec3d axis;
	
	public Quaternion(double w, double x, double y, double z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
		this.axis = new Vec3d(x, y, z);
	}
	
	public Quaternion normalize() {
		double d = Math.sqrt(w * w + x * x + y * y + z * z);
		if (d == 0) {
			return this;
		}
		return new Quaternion(w / d, x / d, y / d, z / d);
	}
	
	public double getW() {
		return w;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public Vec3d getAxisVector() {
		return axis;
	}
}
