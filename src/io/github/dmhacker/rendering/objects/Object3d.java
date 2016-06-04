package io.github.dmhacker.rendering.objects;

import java.util.List;

import io.github.dmhacker.rendering.kdtrees.BoundingBox;
import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public interface Object3d {
	
	public Properties getProperties();

	public double getIntersection(Ray ray);
	
	public List<Vec3d> getBoundingVertices();
	
	public Vec3d getCenter();
	
	public BoundingBox getBoundingBox();
}
