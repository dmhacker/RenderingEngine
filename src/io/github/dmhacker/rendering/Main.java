package io.github.dmhacker.rendering;

import javax.swing.SwingUtilities;

import io.github.dmhacker.rendering.graphics.RenderingFrame;

public class Main {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				RenderingFrame frame = new RenderingFrame();
				frame.start();
			}
		});
	}
	
}
