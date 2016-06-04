package io.github.dmhacker.rendering.objects.meshes;

import java.util.ArrayList;
import java.util.List;

import io.github.dmhacker.rendering.Constants;
import io.github.dmhacker.rendering.objects.Properties;
import io.github.dmhacker.rendering.objects.Triangle;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class RectangularPrism implements Mesh {
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
		facets.add(new Triangle(corner2, corner5, corner1, properties));
		facets.add(new Triangle(corner2, corner5, corner6, properties));
		facets.add(new Triangle(corner2, corner8, corner6, properties));
		facets.add(new Triangle(corner2, corner8, corner4, properties));
		facets.add(new Triangle(corner2, corner3, corner1, properties));
		facets.add(new Triangle(corner2, corner3, corner4, properties));
		facets.add(new Triangle(corner5, corner3, corner1, properties));
		facets.add(new Triangle(corner5, corner3, corner7, properties));
		facets.add(new Triangle(corner8, corner3, corner7, properties));
		facets.add(new Triangle(corner8, corner3, corner4, properties));
		facets.add(new Triangle(corner8, corner5, corner7, properties));
		facets.add(new Triangle(corner8, corner5, corner6, properties));
	}

	public List<Triangle> getFacets() {
		return facets;
	}
}
