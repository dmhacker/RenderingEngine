package io.github.dmhacker.rendering.graphics;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class RenderFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private RayTracer panel;
	
	public RenderFrame() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		int width = screen.width + 4; // So the frame goes to the edge of the screen
		int height = screen.height - 60; // Minus the bottom programs bar
		// width = Math.min(width, height);
		// height = Math.min(width, height);
		
		setSize(width, height);
		setLocation(screen.width / 2 - width / 2, 0);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("3D Rendering Engine");
	
		this.panel = new RayTracer(width, height);
		add(panel);
		
		addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                panel.close();
                System.exit(0);
            }
		});
		
	}
	
	public void start() {
		panel.setVisible(true);
		panel.start();
		setVisible(true);
	}
}
