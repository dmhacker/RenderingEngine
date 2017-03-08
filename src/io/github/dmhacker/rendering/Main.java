package io.github.dmhacker.rendering;

import javax.swing.SwingUtilities;

import io.github.dmhacker.rendering.graphics.RenderingFrame;
import io.github.dmhacker.rendering.options.Options;
import io.github.dmhacker.rendering.options.OptionsFrame;
import io.github.dmhacker.rendering.options.OptionsListener;

public class Main {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				new OptionsFrame(new Options(), new OptionsListener() {

					@Override
					public void onFinish(Options options) {
						RenderingFrame frame = new RenderingFrame(options);
						frame.start();
					}
					
				});
			}
		});
	}
	
}
