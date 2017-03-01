package io.github.dmhacker.rendering.objects;

import java.awt.Color;

public class Properties {
	private Color color;
	private Material material;
	private double kr;
	private double n;
	
	public static Properties create(Color color, Material material) {
		return new Properties(color, material, 0.0, 1.0);
	}
	
	private Properties(Color color, Material material, double reflectivity, double indexOfRefraction) {
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
	
	public Properties setReflectivity(double kr) {
		this.kr = kr;
		return this;
	}
	
	public Properties setRefractiveIndex(double n) {
		this.n = n;
		return this;
	}
}
