package io.github.dmhacker.rendering.objects.meshes;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.dmhacker.rendering.objects.Properties;
import io.github.dmhacker.rendering.objects.Triangle;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class BinarySTLObject implements Mesh {
	private Map<Vec3d, Set<Triangle>> vertexMap;
	private List<Triangle> facets;
	
	public BinarySTLObject(String fileName, Vec3d center, boolean flip, Properties properties) {
		try {
			File file = new File(fileName);
			byte[] bytes = new byte[(int) file.length()];
			FileInputStream inputStream = new FileInputStream(file);
			inputStream.read(bytes);
			inputStream.close();
			
			// Binary STL file decode
			byte[] numFacetsArray = new byte[4];
			int i = 80;
			for (; i < 84; i++) {
				numFacetsArray[i - 80] = bytes[i];
			}
			int numFacets = ByteBuffer.wrap(numFacetsArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
			this.facets = new ArrayList<>(numFacets);
			
			double scalar = 1;
			for (int f = 0; f < numFacets; f++) {
				int counter = i + 12 + 50 * f; // Skip normal vector
				List<Vec3d> vertices = new ArrayList<Vec3d>(3);
				for (int v = 0; v < 3; v++) {
					float[] vertex = new float[3];
					for (int t = 0; t < 3; t++) {
						byte[] floatArr = new byte[4];
						for (int b = 0; b < 4; b++) {
							floatArr[b] = bytes[counter];
							counter++;
						}
						vertex[t] = ByteBuffer.wrap(floatArr).order(ByteOrder.LITTLE_ENDIAN).getFloat();
					}
					vertices.add(new Vec3d(vertex[0], vertex[2], vertex[1]));
					scalar = Math.max(scalar, Math.abs(vertex[0]));
					scalar = Math.max(scalar, Math.abs(vertex[1]));
					scalar = Math.max(scalar, Math.abs(vertex[2]));
				}
				Triangle facet = new Triangle(this, vertices.get(0), vertices.get(1), vertices.get(2), properties);
				facets.add(facet);
			}
			for (Triangle triangle : facets) {
				triangle.scale((flip ? -1.0 : 1.0) / scalar);
				triangle.translate(center);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
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

	public List<Triangle> getFacets() {
		return facets;
	}

	@Override
	public Map<Vec3d, Set<Triangle>> getVertexMap() {
		return vertexMap;
	}
}
