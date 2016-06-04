package io.github.dmhacker.rendering;

import javax.swing.SwingUtilities;

import io.github.dmhacker.rendering.graphics.RenderFrame;

public class Main {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				RenderFrame frame = new RenderFrame();
				frame.start();
			}
		});
	}
	
}
