package io.github.dmhacker.rendering.objects.meshes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import io.github.dmhacker.rendering.objects.Properties;
import io.github.dmhacker.rendering.objects.Triangle;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class STLObject implements Mesh {
	private Map<Vec3d, Set<Triangle>> vertexMap;
	private List<Triangle> facets;
	
	public STLObject(String fileName, Vec3d center, double scale, Properties properties) {
		try {
			Scanner sc = new Scanner(new FileReader(fileName));
			String start = sc.next();
			sc.close();
			if ("solid".equals(start)) {
				parseASCII(fileName, center, scale, properties);
			}
			else {
				parseBinary(fileName, center, scale, properties);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void parseASCII(String fileName, Vec3d center, double scale, Properties properties) throws Exception {
		this.facets = new ArrayList<>();
		Scanner sc = new Scanner(new FileReader(fileName));
		sc.useDelimiter("normal");
		List<Double> num;
		double scalar = 1.0;
		while(sc.hasNext()){
			num = new ArrayList<Double>(12);
			String facet = sc.next();
			String[] bits = facet.split(" ");
			for(String bit: bits){
				try{
					num.add(Double.parseDouble(bit.trim()));
				} catch(Exception e){
						
				}
			}
			if(num.size() == 12){
				Vec3d[] pos = new Vec3d[] {
						new Vec3d(num.get(3), num.get(5), num.get(4)), 
						new Vec3d(num.get(6), num.get(8), num.get(7)), 
						new Vec3d(num.get(9), num.get(11), num.get(10))
					};
				facets.add(new Triangle(this, pos[0], pos[1], pos[2], properties));
				for (Vec3d vertex : pos) {
					scalar = Math.max(scalar, vertex.getX());
					scalar = Math.max(scalar, vertex.getY());
					scalar = Math.max(scalar, vertex.getZ());
				}
			}
		}
		for (Triangle triangle : facets) {
			triangle.scale(scale / scalar);
			triangle.translate(center);
		}
		sc.close();
		
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
	
	private void parseBinary(String fileName, Vec3d center, double scale, Properties properties) throws Exception {
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
			triangle.scale(scale / scalar);
			triangle.translate(center);
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
