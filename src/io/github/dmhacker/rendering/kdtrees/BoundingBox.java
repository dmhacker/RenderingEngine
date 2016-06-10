package io.github.dmhacker.rendering.kdtrees;

import java.util.ArrayList;
import java.util.List;

import io.github.dmhacker.rendering.Constants;
import io.github.dmhacker.rendering.objects.Object3d;
import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class BoundingBox {
	private double[] boundingBox;
	
	public BoundingBox() {
		this.boundingBox = new double[6];
	}
	
	public BoundingBox(List<Vec3d> vecs) {
		this.boundingBox = new double[] {
				vecs.get(0).getX(),
				vecs.get(0).getX(),
				vecs.get(0).getY(),
				vecs.get(0).getY(),
				vecs.get(0).getZ(),
				vecs.get(0).getZ(),
		}; 
		for (int i = 1; i < vecs.size(); i++) {
			Vec3d vec = vecs.get(i);
			double x = vec.getX();
			double y = vec.getY();
			double z = vec.getZ();
			if (x < boundingBox[0]) {
				boundingBox[0] = x;
			}
			if (x > boundingBox[1]) {
				boundingBox[1] = x;
			}
			if (y < boundingBox[2]) {
				boundingBox[2] = y;
			}
			if (y > boundingBox[3]) {
				boundingBox[3] = y;
			}
			if (z < boundingBox[4]) {
				boundingBox[4] = z;
			}
			if (z > boundingBox[5]) {
				boundingBox[5] = z;
			}
		}
		for (int i = 0; i < 6; i++) {
			if (i % 2 == 0) {
				boundingBox[i] -= Constants.EPSILON;
			}
			else {
				boundingBox[i] += Constants.EPSILON;
			} 
		}
	}
	
	public boolean contains(Vec3d point) {
		return 	point.getX() >= boundingBox[0] && 
				point.getX() <= boundingBox[1] && 
				point.getY() >= boundingBox[2] &&
				point.getY() <= boundingBox[3] &&
				point.getZ() >= boundingBox[4] &&
				point.getZ() <= boundingBox[5];  
	}
	
	public boolean isIntersecting(Ray ray) {
		Vec3d origin = ray.getOrigin();
		Vec3d dirFrac = ray.getInverseDirection();
		
		double t1 = (boundingBox[0] - origin.getX()) * dirFrac.getX();
		double t2 = (boundingBox[1] - origin.getX()) * dirFrac.getX();
		double t3 = (boundingBox[2] - origin.getY()) * dirFrac.getY();
		double t4 = (boundingBox[3] - origin.getY()) * dirFrac.getY();
		double t5 = (boundingBox[4] - origin.getZ()) * dirFrac.getZ();
		double t6 = (boundingBox[5] - origin.getZ()) * dirFrac.getZ();
		
		double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
		double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
		
		return tmin <= tmax;
	}
	
	public static BoundingBox fromObjects(List<Object3d> objects) {
		List<Vec3d> vecs = new ArrayList<Vec3d>();
		for (Object3d obj : objects) {
			vecs.addAll(obj.getBoundingVertices());
		}
		return new BoundingBox(vecs);
	}
}
