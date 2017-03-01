package io.github.dmhacker.rendering.graphics;

import javax.swing.JPanel;

public abstract class RenderingEngine extends JPanel {
	private static final long serialVersionUID = 1L;

	public abstract void start();
	
	public abstract void close();
	
	public abstract void render();
}
