package io.github.dmhacker.rendering.options;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;

import io.github.dmhacker.rendering.graphics.RenderingEngine;
import io.github.dmhacker.rendering.graphics.impl.BidirectionalPathTracer;
import io.github.dmhacker.rendering.graphics.impl.RayTracer;

public class Options {
	public static Map<String, Class<? extends RenderingEngine>> RENDERING_ENGINES;
	public static Map<String, Map<String, Boolean>> RENDERING_ENGINE_OPTIONS;
	
	static {
		RENDERING_ENGINES = new HashMap<>();
		RENDERING_ENGINES.put("Ray Tracer", RayTracer.class);
		RENDERING_ENGINES.put("Bidirectional Path Tracer", BidirectionalPathTracer.class);
		
		RENDERING_ENGINE_OPTIONS = new HashMap<>();
		RENDERING_ENGINE_OPTIONS.put("Ray Tracer", new HashMap<String, Boolean>() {
			private static final long serialVersionUID = 1L;

		{
			put("kd-tree", true);
			put("Vertex normal interpolation", true);
			put("Wireframe rendering", true);
			put("Anti-aliasing", false);
			put("Soft shadows", false);
		}});
		RENDERING_ENGINE_OPTIONS.put("Bidirectional Path Tracer", new HashMap<String, Boolean>() {
			private static final long serialVersionUID = 1L;

		{
			put("kd-tree", true);
			put("Vertex normal interpolation", true);
		}});
	}
	
	private String renderingEngine;
	private Map<String, Map<String, JCheckBox>> engineConfig;
	
	public Options() {
		this.renderingEngine = "Ray Tracer";
		this.engineConfig = new HashMap<>();
		for (Entry<String, Map<String, Boolean>> entry : RENDERING_ENGINE_OPTIONS.entrySet()) {
			String entryRenderingEngine = entry.getKey();
			Map<String, JCheckBox> buttons = new HashMap<>();
			for (Entry<String, Boolean> entry2 : entry.getValue().entrySet()) {
				JCheckBox button = new JCheckBox();
				button.setFont(new Font("Verdana", Font.PLAIN, 18));
				button.setText(entry2.getKey());
				button.setActionCommand(entry2.getKey());
				button.setSelected(entry2.getValue());
				buttons.put(entry2.getKey(), button);
			}
			engineConfig.put(entryRenderingEngine, buttons);
		}
	}
	
	public String getSelectedEngine() {
		return renderingEngine;
	}
	
	public void setSelectedEngine(String renderingEngine) {
		this.renderingEngine = renderingEngine;
	}
	
	public Map<String, JCheckBox> getSelectedEngineConfiguration() {
		return engineConfig.get(renderingEngine);
	}
}
