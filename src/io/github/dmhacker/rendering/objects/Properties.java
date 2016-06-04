package io.github.dmhacker.rendering.objects;

import java.awt.Color;

public class Properties {
	private Color color;
	private Material material;
	private double kr;
	private double n;
	
	public Properties() {
		this(Color.WHITE, Material.OPAQUE, 0.0, 1.0);
	}
	
	public Properties(Color color, Material material, double reflectivity, double indexOfRefraction) {
		this.color = color;
		this.material = material;
		this.kr = reflectivity;
		this.n = indexOfRefraction;
	}
	
	public Color getColor() {
		return color;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public double getReflectivity() {
		return kr;
	}
	
	public double getRefractiveIndex() {
		return n;
	}
}
