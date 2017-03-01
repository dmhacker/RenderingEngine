package io.github.dmhacker.rendering;

import java.awt.Color;

import io.github.dmhacker.rendering.objects.Material;

public class Constants {
	public static final double EPSILON = 1E-7;
	
	public static final Color BACKGROUND = Color.BLACK;
	public static final float[] BACKGROUND_RGB = toColorArray(BACKGROUND);
	
	public static final Color DEFAULT_OBJECT_COLOR = Color.WHITE;
	public static final Material DEFAULT_OBJECT_MATERIAL = Material.OPAQUE;

	private static float[] toColorArray(Color color) {
		return new float[] {
				color.getRed(),
				color.getGreen(),
				color.getBlue()
		};
	} 
}
