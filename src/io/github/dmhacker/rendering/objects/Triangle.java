package io.github.dmhacker.rendering.objects;

import java.util.ArrayList;
import java.util.List;

import io.github.dmhacker.rendering.Constants;
import io.github.dmhacker.rendering.graphics.RayTracer;
import io.github.dmhacker.rendering.kdtrees.BoundingBox;
import io.github.dmhacker.rendering.objects.meshes.Mesh;
import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class Triangle implements Object3d {
	private Mesh mesh;
	private List<Vec3d> vertices;
	private Vec3d face;
	private Vec3d faceNormal;
	private Vec3d centroid;
	private Properties properties;
	private BoundingBox bbox;
	
	public Triangle(Mesh mesh, Vec3d v1, Vec3d v2, Vec3d v3, Properties properties) {
		
		this.mesh = mesh;

		this.vertices = new ArrayList<Vec3d>() {
			private static final long serialVersionUID = 1L;

		{
			add(v1);
			add(v2);
			add(v3);
		}};
		
		this.properties = properties;
		
		calculate();
	}
	
	private void calculate() {
		this.bbox = new BoundingBox(vertices);
		
		Vec3d v1 = vertices.get(0);
		Vec3d v2 = vertices.get(1);
		Vec3d v3 = vertices.get(2);
		Vec3d e1 = v2.subtract(v1);
		Vec3d e2 = v3.subtract(v1);
		this.face = e1.cross(e2);
		this.faceNormal = face.normalize();
		
		this.centroid = v1.add(v2).add(v3).divide(3.0);
	}
	
	public List<Vec3d> getVertices() {
		return vertices;
	}
	
	public void translate(Vec3d translation) {
		vertices.set(0, vertices.get(0).add(translation));
		vertices.set(1, vertices.get(1).add(translation));
		vertices.set(2, vertices.get(2).add(translation));
		calculate();
	}
	
	public void scale(double scalar) {
		vertices.set(0, vertices.get(0).multiply(scalar));
		vertices.set(1, vertices.get(1).multiply(scalar));
		vertices.set(2, vertices.get(2).multiply(scalar));
		calculate();
	}
	
	public Vec3d getNormal(Ray ray, Vec3d intersection) {
		if (mesh == null || !RayTracer.VERTEX_NORMAL_INTERPOLATION) {
			return getFaceNormal(ray);
		}
		return getVertexNormal(ray, intersection);
	}
	
	public Vec3d getFaceNormal(Ray ray) {
		if (ray.getDirection().dotProduct(faceNormal) > ray.getDirection().dotProduct(faceNormal.negative())) {
			return faceNormal.negative();
		}
		return faceNormal;
	}
	
	public Vec3d getVertexNormal(Ray ray, Vec3d intersection) {
		Vec3d n = new Vec3d();
		for (Vec3d v : vertices) {
			Vec3d vertexNormal = new Vec3d();
			for (Triangle t : mesh.getVertexMap().get(v)) {
				vertexNormal = vertexNormal.add(t.faceNormal);
			}
			n = n.add(vertexNormal.divide(v.subtract(intersection).distance()));
		}
		n = n.normalize();
		if (ray.getDirection().dotProduct(n) > ray.getDirection().dotProduct(n.negative())) {
			return n.negative();
		}
		return n;
	}
	
	public Vec3d getCenter() {
		return centroid;
	}
	
	@Override
	public double getIntersection(Ray ray) {
		return mollerTrumboreIntersection(ray);
	}
	
	protected double mollerTrumboreIntersection(Ray ray) {
		Vec3d v1 = vertices.get(0);
		Vec3d v2 = vertices.get(1);
		Vec3d v3 = vertices.get(2);
		Vec3d v0v1 = v2.subtract(v1); 
		Vec3d v0v2 = v3.subtract(v1) ;
		Vec3d pvec = ray.getDirection().cross(v0v2); 
		double det = v0v1.dotProduct(pvec); 
		if (Math.abs(det) < Constants.EPSILON) 
			return -1; 
		double invDet = 1.0 / det; 
		 
		Vec3d tvec = ray.getOrigin().subtract(v1); 
		double u = tvec.dotProduct(pvec) * invDet; 
		if (u < 0 || u > 1) 
			return -1; 
		 
		Vec3d qvec = tvec.cross(v0v1); 
		double v = ray.getDirection().dotProduct(qvec) * invDet; 
		if (v < 0 || u + v > 1) 
			return -1; 
		 
		double t = v0v2.dotProduct(qvec) * invDet; 
		if (t < Constants.EPSILON)
			return -1;
		
		return t;
	}
	
	protected double simpleIntersection(Ray ray) {
		if (bbox.intersects(ray) == -1) {
			return -1;
		}
		
		Vec3d norm = getFaceNormal(ray).negative();
		
		// Does the ray intersect the triangular plane?
		double denom = norm.dotProduct(ray.getDirection()); 
	    if (denom <= Constants.EPSILON) {
	    	return -1;
	    } 
		double t = getCenter().subtract(ray.getOrigin()).dotProduct(norm) / denom;
		if (t < Constants.EPSILON) {
    		return -1;
		}
	    
	    // Determine if point is within the triangle by comparing angles between edges
	    Vec3d intersection = ray.evaluate(t);
	    for (int i = 0; i < 3; i++) {
	    	Vec3d u = vertices.get(i);
	    	Vec3d v = vertices.get((i + 1) % 3);
	    	Vec3d w = vertices.get((i + 2) % 3);
	    	if (!sameSide(intersection, u, v, w)) {
	    		return -1;
	    	}
	    }
		return t;
	}
	
	public boolean sameSide(Vec3d p1, Vec3d p2, Vec3d a, Vec3d b) {
		Vec3d cp1 = b.subtract(a).cross(p1.subtract(a));
		Vec3d cp2 = b.subtract(a).cross(p2.subtract(a));
		return cp1.dotProduct(cp2) >= 0;
	}
	
	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public List<Vec3d> getBoundingVertices() {
		return vertices;
	}

	public String toString() {
		return "Triangle"+vertices.toString();
	}

	@Override
	public BoundingBox getBoundingBox() {
		return bbox;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Triangle) {
			Triangle t = (Triangle) o;
			return  vertices.get(0).equals(t.vertices.get(0)) &&
					vertices.get(1).equals(t.vertices.get(1)) &&
					vertices.get(2).equals(t.vertices.get(2));
		}
		return false;
	}
}
