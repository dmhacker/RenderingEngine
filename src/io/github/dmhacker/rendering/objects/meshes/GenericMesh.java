package io.github.dmhacker.rendering.objects.meshes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.dmhacker.rendering.objects.Properties;
import io.github.dmhacker.rendering.objects.Triangle;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class GenericMesh implements Mesh {
	private Map<Vec3d, Set<Triangle>> vertexMap;
	private List<Triangle> facets;
	
	public GenericMesh(List<Triangle> facets) {
		this.facets = facets;
		
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

	@Override
	public List<Triangle> getFacets() {
		return facets;
	}

	@Override
	public Map<Vec3d, Set<Triangle>> getVertexMap() {
		return vertexMap;
	}
}
