package io.github.dmhacker.rendering.graphics.impl;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JCheckBox;

public enum RayTracerOption {
	
	KD_TREE("kd-tree", true),
	VERTEX_NORMAL_INTERPOLATION("Vertex normal interpolation", true),
	WIREFRAME("Wireframe rendering", true),
	ANTI_ALIASING("Anti-aliasing", false),
	SOFT_SHADOWS("Soft shadows", false),
	VIEW_LIGHT_SOURCES("View light sources", false);
	
	private String text;
	private boolean def;
	
	private RayTracerOption(String text, boolean def) {
		this.text = text;
		this.def = def;
	}
	
	public String getText() {
		return text;
	}
	
	public boolean getDefaultValue() {
		return def;
	}
	
	private static Map<RayTracerOption, JCheckBox> options;
	
	static {
		options = new EnumMap<RayTracerOption, JCheckBox>(RayTracerOption.class);
		for (RayTracerOption option : RayTracerOption.values()) {
			JCheckBox checkBox = new JCheckBox(option.getText());
			checkBox.setSelected(option.getDefaultValue());
			options.put(option, checkBox);
		}
	}
	
	public static Collection<JCheckBox> getCheckboxes() {
		return options.values();
	}
	
	public boolean get() {
		return options.get(this).isSelected();
	}
}
