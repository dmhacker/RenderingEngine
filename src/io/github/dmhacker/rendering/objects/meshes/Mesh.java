package io.github.dmhacker.rendering.objects.meshes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.dmhacker.rendering.objects.Properties;
import io.github.dmhacker.rendering.objects.Triangle;
import io.github.dmhacker.rendering.vectors.Vec3d;

public interface Mesh {

	public List<Triangle> getFacets();
	
	public Map<Vec3d, Set<Triangle>> getVertexMap();
	
	public Mesh translate(Vec3d translation);
	
	public Mesh scale(double scaleBy);
	
	public Mesh setProperties(Properties properties);
}
