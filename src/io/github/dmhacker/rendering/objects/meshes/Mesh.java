package io.github.dmhacker.rendering.objects.meshes;

import java.util.List;

import io.github.dmhacker.rendering.objects.Triangle;

public interface Mesh {

	public List<Triangle> getFacets();
}
