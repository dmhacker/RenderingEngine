package io.github.dmhacker.rendering.objects.meshes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.dmhacker.rendering.Constants;
import io.github.dmhacker.rendering.objects.Properties;
import io.github.dmhacker.rendering.objects.Triangle;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class RectangularPrism implements Mesh {
	private Map<Vec3d, Set<Triangle>> vertexMap;
	private List<Triangle> facets;
	
	public RectangularPrism(Vec3d center, double width, double height, double length, Properties properties) {
		this.facets = new ArrayList<>();
		Vec3d corner1 = center.add(-width / 2 - Constants.EPSILON * width, -height / 2 - Constants.EPSILON * height, -length / 2 - Constants.EPSILON * length);
		Vec3d corner2 = center.add(width / 2 + Constants.EPSILON * width, -height / 2 - Constants.EPSILON * height, -length / 2 - Constants.EPSILON * length);
		Vec3d corner3 = center.add(-width / 2 - Constants.EPSILON * width, -height / 2 - Constants.EPSILON * height, length / 2 + Constants.EPSILON * length);
		Vec3d corner4 = center.add(width / 2 + Constants.EPSILON * width, -height / 2 - Constants.EPSILON * height, length / 2 + Constants.EPSILON * length);
		Vec3d corner5 = center.add(-width / 2 - Constants.EPSILON * width, height / 2 + Constants.EPSILON * height, -length / 2 - Constants.EPSILON * length);
		Vec3d corner6 = center.add(width / 2 + Constants.EPSILON * width, height / 2 + Constants.EPSILON * height, -length / 2 - Constants.EPSILON * length);
		Vec3d corner7 = center.add(-width / 2 - Constants.EPSILON * width, height / 2 + Constants.EPSILON * height, length / 2 + Constants.EPSILON * length);
		Vec3d corner8 = center.add(width / 2 + Constants.EPSILON * width, height / 2 + Constants.EPSILON * height, length / 2 + Constants.EPSILON * length);
		facets.add(new Triangle(this, corner2, corner5, corner1, properties));
		facets.add(new Triangle(this, corner2, corner5, corner6, properties));
		facets.add(new Triangle(this, corner2, corner8, corner6, properties));
		facets.add(new Triangle(this, corner2, corner8, corner4, properties));
		facets.add(new Triangle(this, corner2, corner3, corner1, properties));
		facets.add(new Triangle(this, corner2, corner3, corner4, properties));
		facets.add(new Triangle(this, corner5, corner3, corner1, properties));
		facets.add(new Triangle(this, corner5, corner3, corner7, properties));
		facets.add(new Triangle(this, corner8, corner3, corner7, properties));
		facets.add(new Triangle(this, corner8, corner3, corner4, properties));
		facets.add(new Triangle(this, corner8, corner5, corner7, properties));
		facets.add(new Triangle(this, corner8, corner5, corner6, properties));
		
		this.vertexMap = new HashMap<Vec3d, Set<Triangle>>();
		for (Triangle triangle : facets) {
			for (Vec3d vertex : triangle.getVertices()) {
				if (vertexMap.containsKey(vertex)) {
					vertexMap.get(vertex).add(triangle);
				}
				else {
					Set<Triangle> triangles = new HashSet<Triangle>();
					triangles.add(triangle);
					vertexMap.put(vertex, triangles);
				}
			}
		}
	}
	
	public Mesh translate(Vec3d translation) {
		for (Triangle facet : facets) {
			facet.translate(translation);
		}
		vertexMap.clear();
		for (Triangle triangle : facets) {
			for (Vec3d vertex : triangle.getVertices()) {
				if (vertexMap.containsKey(vertex)) {
					vertexMap.get(vertex).add(triangle);
				}
				else {
					Set<Triangle> triangles = new HashSet<Triangle>();
					triangles.add(triangle);
					vertexMap.put(vertex, triangles);
				}
			}
		}
		return this;
	}
	
	public Mesh scale(double scaleBy) {
		for (Triangle facet : facets) {
			facet.scale(scaleBy);
		}
		vertexMap.clear();
		for (Triangle triangle : facets) {
			for (Vec3d vertex : triangle.getVertices()) {
				if (vertexMap.containsKey(vertex)) {
					vertexMap.get(vertex).add(triangle);
				}
				else {
					Set<Triangle> triangles = new HashSet<Triangle>();
					triangles.add(triangle);
					vertexMap.put(vertex, triangles);
				}
			}
		}
		return this;
	}

	@Override
	public Mesh setProperties(Properties properties) {
		for (Triangle facet : facets) {
			facet.setProperties(properties);
		}
		return this;
	}

	public List<Triangle> getFacets() {
		return facets;
	}

	@Override
	public Map<Vec3d, Set<Triangle>> getVertexMap() {
		return vertexMap;
	}
}
