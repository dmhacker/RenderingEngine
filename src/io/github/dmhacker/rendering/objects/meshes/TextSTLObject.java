package io.github.dmhacker.rendering.objects.meshes;

import java.io.FileReader;
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

public class TextSTLObject implements Mesh {
	private Map<Vec3d, Set<Triangle>> vertexMap;
	private List<Triangle> facets;
	
	public TextSTLObject(String fileName, Vec3d center, boolean flip, Properties properties) {
		try {
			this.facets = new ArrayList<>();
			Scanner sc = new Scanner(new FileReader(fileName));
			sc.useDelimiter("normal");
			List<Double> num;
			double scalar = 1.0;
			while(sc.hasNext()){
				num = new ArrayList<Double>(12);
				String facet = sc.next();
				String[] bits = facet.split("[^0123456789.-]");
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
				triangle.scale((flip ? -1.0 : 1.0) / scalar);
				triangle.translate(center);
			}
			sc.close();
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
