package io.github.dmhacker.rendering.graphics.accl;

import io.github.dmhacker.rendering.objects.Object3d;

public class KDNodeResults {
	private Object3d obj;
	private double t;
	
	public KDNodeResults() {
		this(null, Double.MAX_VALUE);
	}
	
	public KDNodeResults(Object3d obj, double t) {
		this.obj = obj;
		this.t = t;
	}
	
	public Object3d getObject() {
		return obj;
	}
	
	public double getIntersectionLength() {
		return t;
	}
}
