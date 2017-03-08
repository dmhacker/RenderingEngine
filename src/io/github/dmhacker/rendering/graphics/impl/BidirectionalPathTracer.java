package io.github.dmhacker.rendering.graphics.impl;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import io.github.dmhacker.rendering.Constants;
import io.github.dmhacker.rendering.graphics.RenderingEngine;
import io.github.dmhacker.rendering.graphics.accl.KDNode;
import io.github.dmhacker.rendering.graphics.accl.KDNodeResults;
import io.github.dmhacker.rendering.objects.Light;
import io.github.dmhacker.rendering.objects.Object3d;
import io.github.dmhacker.rendering.objects.Scene;
import io.github.dmhacker.rendering.options.Options;
import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class BidirectionalPathTracer extends RenderingEngine {
	private static final long serialVersionUID = 1L;
	
	//================================================================================
	// Rendering Options
	//================================================================================
	private static final int THREADS = Runtime.getRuntime().availableProcessors();
	
	//================================================================================
	// Lighting Properties
	//================================================================================
	private static final int RECURSIVE_DEPTH = 9;
	
	//================================================================================
	// Variables
	//================================================================================
	private AtomicBoolean running;
	private AtomicBoolean rendering;
	
	private BufferedImage image;
	private final int width;
	private final int height;
	private final int area;
	
	private Scene scene;
	private Map<String, JCheckBox> engineConfig;
	
	private KDNode tree;
	
	public BidirectionalPathTracer(int width, int height, Scene scene, Options options) {
		setFocusable(true);
		requestFocusInWindow();
				
		this.running = new AtomicBoolean();
		this.rendering = new AtomicBoolean();
		
		this.width = width;
		this.height = height;
		this.area = width * height;
		
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		// Detect if key was pressed (for saving fractals)
		addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {}

			@Override
			public void keyReleased(KeyEvent arg0) {}

			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == 's') {
					try {
						File imageFile = new File("renders/"+UUID.randomUUID()+".png");
						ImageIO.write(image, "png", imageFile);
						JOptionPane.showMessageDialog(null, "Render saved as: "+imageFile.getName());
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			
		});
		
		this.scene = scene;
		this.engineConfig = options.getSelectedEngineConfiguration();
		
		if (engineConfig.get("kd-tree").isSelected()) {
			long timestamp = System.currentTimeMillis();
			this.tree = KDNode.build(null, scene.getObjects(), 0);
			long generationTime = System.currentTimeMillis() - timestamp;
			System.out.println("kd-tree generation: "+generationTime+"ms ("+Math.round(generationTime / 1000.0)+"s)");
		}
	}
	
	public void start() {
		running.set(true);
		render();
	}
	
	public void close() {
		running.set(false);
	}

	public void render() {
		new Thread() {
			
			@Override
			public void run() {
				renderInternal();
			}
			
		}.start();
	}
	
	private void renderInternal() {
		/*
		long timestamp = System.currentTimeMillis();
		rendering.set(true);
		AtomicInteger threadsCompleted = new AtomicInteger();
		AtomicInteger pixel = new AtomicInteger();
		final double scale = 1.0 / scene.getZoom() / Math.min(width, height);
		final double sampleLength = scale / ANTIALIASING_SAMPLE_SUBDIVISIONS;
		for (int t = 0; t < THREADS; t++) {
			new Thread() {
				
				@Override
				public void run() {
					int current;
					while ((current = pixel.getAndIncrement()) < area) {
						int px = current % width;
						int py = current / width;
						double x = (px - width / 2) * scale;
						double y = (-py + height / 2) * scale;
						float[] rawColor;
						// In both cases, it is assumed that the image plane is at z = 0
						if (RayTracerOption.ANTI_ALIASING.get()) {
							rawColor = new float[3];
							double minX = x - scale / 2;
							double minY = y - scale / 2;
							for (int i = 0; i < ANTIALIASING_SAMPLE_SUBDIVISIONS; i++) {
								for (int j = 0; j < ANTIALIASING_SAMPLE_SUBDIVISIONS; j++) {
									double nx = minX + i * sampleLength;
									double ny = minY + j * sampleLength;
									Vec3d point;
									point = new Vec3d(nx + Math.random() * sampleLength, ny + Math.random() * sampleLength, 0).rotate(scene.getCameraRotationQuaternion());
									float[] sample = cast(Ray.between(scene.getCamera(), point), px, py);
									for (int k = 0; k < 3; k++)
										rawColor[k] += gaussian(i - GAUSSIAN_MEAN, j - GAUSSIAN_MEAN) * sample[k];
								}
							}
							for (int i = 0; i < 3; i++)
								rawColor[i] *= GAUSSIAN_SCALE;
						}
						else {
							Vec3d point;
							point = new Vec3d(x, y, scene.getCamera().getZ() + scene.getCameraSize()).rotate(scene.getCameraRotationQuaternion());
							rawColor = cast(Ray.between(scene.getCamera(), point), px, py);
						}
						image.setRGB(px, py, (255 << 24) | ((int) Math.min(rawColor[0], 255) << 16) | ((int) Math.min(rawColor[1], 255) << 8) | ((int) Math.min(rawColor[2], 255)));
						repaint(px, py, 1, 1);
					}
					threadsCompleted.incrementAndGet();
				}
				
			}.start();
		}
		
		// Busy waiting
		while (threadsCompleted.get() < THREADS);
		
		rendering.set(false);
		
		long renderTime = System.currentTimeMillis() - timestamp;
		System.out.println("Render time: "+renderTime+"ms ("+Math.round(renderTime / 1000.0)+"s)");
		*/
	}
	
	private float[] cast(Ray ray) {
		return cast(ray, 0);
	}

	private float[] cast(Ray ray, int depth) {
		float[] colors = {0f, 0f, 0f};
		
		if (depth == RECURSIVE_DEPTH) {
			return colors;
		}
		
		Object3d closest = null;
		double tMin = Double.MAX_VALUE;
		
		if (engineConfig.get("kd-tree").isSelected()) {
			KDNodeResults ret = KDNode.parseTree(tree, ray, false);
			closest = ret.getObject();
			tMin = ret.getIntersectionLength();
		}
		else {
			for (Object3d obj : scene.getObjects()) {
				double t = obj.getIntersection(ray);
				if (t > 0 && t < tMin) {
					tMin = t;
					closest = obj;
				}
			}
		}
		
		if (closest == null) {
			return Constants.BACKGROUND_RGB;
		}
		
		Vec3d intersectionPoint = ray.evaluate(tMin);
		
		return colors;
	}
}
