package io.github.dmhacker.rendering.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import io.github.dmhacker.rendering.kdtrees.KDNode;
import io.github.dmhacker.rendering.objects.Light;
import io.github.dmhacker.rendering.objects.Material;
import io.github.dmhacker.rendering.objects.Object3d;
import io.github.dmhacker.rendering.objects.Properties;
import io.github.dmhacker.rendering.objects.Sphere;
import io.github.dmhacker.rendering.objects.Triangle;
import io.github.dmhacker.rendering.objects.meshes.BinarySTLObject;
import io.github.dmhacker.rendering.objects.meshes.Mesh;
import io.github.dmhacker.rendering.objects.meshes.TextSTLObject;
import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class RayTracer extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final boolean KD_TREE_ENABLED = true;
	private static final boolean ANTIALIASING_ENABLED = true;
	
	private static final int ANTIALIASING_SAMPLE_SUBDIVISIONS = 3;
	
	private static final int THREADS = Runtime.getRuntime().availableProcessors();
	
	private static final double KD = 0.9; // Light diffusion
	private static final double KA = 0.1; // Light ambiance
	
	private static final int RECURSIVE_DEPTH = 6;
	
	private static final Color BACKGROUND = Color.BLACK;
	private static final float[] BACKGROUND_COLOR = new float[] {
			BACKGROUND.getRed(), BACKGROUND.getGreen(), BACKGROUND.getBlue()
	};
	
	private AtomicBoolean running;
	private AtomicBoolean rendering;
	
	private BufferedImage image;
	private final int width;
	private final int height;
	private final int area;
	
	private Vec3d camera;
	private List<Object3d> objects;
	private List<Light> lights;
	
	private KDNode tree;
	
	public RayTracer(int width, int height) {
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
		
		this.camera = new Vec3d(0, 0.3, -1);
		
		this.lights = new ArrayList<Light>() {
			private static final long serialVersionUID = 1L;

		{
			add(new Light(new Vec3d(0, 4, -3), 0.5));
		}};
		
		this.objects = new ArrayList<>();

		Properties sphericalMirror = new Properties(new Color(221, 207, 153), Material.SHINY, 0.7, 1);
		Sphere sp1 = new Sphere(-3, 0, 6, 1, sphericalMirror);
		Sphere sp2 = new Sphere(0, 0, 6, 1, sphericalMirror);
		Sphere sp3 = new Sphere(3, 0, 6, 1, sphericalMirror);
		
		objects.add(sp1);
		objects.add(sp2);
		objects.add(sp3);

		// Regular floor
		double surfaceY = -1;
		Vec3d topLeft = new Vec3d(-10000, surfaceY, 10000);
		Vec3d bottomLeft = new Vec3d(-10000, surfaceY, -10000);
		Vec3d topRight = new Vec3d(10000, surfaceY, 10000);
		Vec3d bottomRight = new Vec3d(10000, surfaceY, -10000);
		Properties floorProperties = new Properties(new Color(86, 47, 14), Material.SHINY, 0.5, 1);
		
		Triangle topLeftPortion = new Triangle(bottomLeft, topLeft, topRight, floorProperties);
		Triangle bottomRightPortion = new Triangle(bottomLeft, bottomRight, topRight, floorProperties);
		
		objects.add(topLeftPortion);
		objects.add(bottomRightPortion);
		
		// Mesh mobius = new TextSTLObject("C:/Users/David Hacker/3D Objects/mobius.stl", new Vec3d(0, -0.8, 2), true, new Properties(new Color(140, 21, 21), Material.OPAQUE, 1, 1));
		// Mesh mobius2 = new TextSTLObject("C:/Users/David Hacker/3D Objects/mobius_2.stl", new Vec3d(0, -0.8, 1.5), false, new Properties(new Color(140, 21, 21), Material.OPAQUE, 1, 1));
		// Mesh teapot = new BinarySTLObject("C:/Users/David Hacker/3D Objects/teapot.stl", new Vec3d(0, -1.0, 2), false, new Properties(new Color(255, 189, 23), Material.OPAQUE, 0.5, 1));
		// Mesh tieFront = new BinarySTLObject("C:/Users/David Hacker/3D Objects/TIE-front.stl", new Vec3d(0, -1.0, 1.3), false, new Properties(new Color(255, 189, 23), Material.OPAQUE, 0.5, 1));
		// Mesh torusKnot = new TextSTLObject("C:/Users/David Hacker/3D Objects/TripleTorus.stl", new Vec3d(0, -0.5, 2), false, new Properties(new Color(255, 189, 23), Material.OPAQUE, 0.5, 1.0));
		// Mesh stanfordBunny = new BinarySTLObject("C:/Users/David Hacker/3D Objects/StanfordBunny.stl", new Vec3d(0, -1.05, 1.5), false, new Properties(new Color(140, 21, 21), Material.OPAQUE, 0.5, 1));
		// Mesh stanfordDragon = new BinarySTLObject("C:/Users/David Hacker/3D Objects/StanfordDragon.stl", new Vec3d(0, -0.3, 1.5), false, new Properties(new Color(140, 21, 21), Material.OPAQUE, 0.3, 1));
		// Mesh langtonsAnt = new BinarySTLObject("C:/Users/David Hacker/3D Objects/langtonsant.stl", new Vec3d(0, -0.3, 1.5), false, new Properties(new Color(140, 21, 21), Material.OPAQUE, 0.3, 1));
		Mesh mandelbulb = new BinarySTLObject("C:/Users/David Hacker/3D Objects/mandelbulb_wimpy.stl", new Vec3d(-0.5, -1, 1), false, new Properties(new Color(140, 21, 21), Material.OPAQUE, 0.3, 1));
		
		addMesh(mandelbulb);

		System.out.println("Rendering "+objects.size()+" polygons/spheres ...");

		if (KD_TREE_ENABLED) {
			long timestamp = System.currentTimeMillis();
			this.tree = KDNode.build(null, objects, 0);
			System.out.println("Tree generation: "+(System.currentTimeMillis() - timestamp)+"ms");
			// System.out.println("Tree: "+tree);
		}
		
	}
	
	public void addMesh(Mesh mesh) {
		objects.addAll(mesh.getFacets());
	}
	
	public void start() {
		running.set(true);
		render();
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
		long timestamp = System.currentTimeMillis();
		rendering.set(true);
		AtomicInteger threadsCompleted = new AtomicInteger();
		AtomicInteger pixel = new AtomicInteger();
		final double scale = 1.0 / Math.min(width, height);
		final double sampleLength = scale / ANTIALIASING_SAMPLE_SUBDIVISIONS;
		final int samples = ANTIALIASING_SAMPLE_SUBDIVISIONS * ANTIALIASING_SAMPLE_SUBDIVISIONS;
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
						if (ANTIALIASING_ENABLED) {
							rawColor = new float[3];
							double minX = x - scale / 2;
							double minY = y - scale / 2;
							for (int i = 0; i < ANTIALIASING_SAMPLE_SUBDIVISIONS; i++) {
								for (int j = 0; j < ANTIALIASING_SAMPLE_SUBDIVISIONS; j++) {
									double nx = minX + i * sampleLength;
									double ny = minY + j * sampleLength;
									Vec3d point = new Vec3d(nx + Math.random() * sampleLength, ny + Math.random() * sampleLength, 0);
									float[] sample = cast(Ray.between(camera, point), 0);
									for (int k = 0; k < 3; k++)
										rawColor[k] += sample[k];
								}
							}
							for (int k = 0; k < 3; k++)
								rawColor[k] = rawColor[k] / samples;
						}
						else {
							Vec3d point = new Vec3d(x, y, 0); 
							rawColor = cast(Ray.between(camera, point), 0);
						}
						image.setRGB(px, py, (255 << 24) | ((int) Math.min(rawColor[0], 255) << 16) | ((int) Math.min(rawColor[1], 255) << 8) | ((int) Math.min(rawColor[2], 255)));
						repaint(px, py, 1, 1);
					}
					threadsCompleted.incrementAndGet();
				}
				
			}.start();
		}
		while (threadsCompleted.get() < THREADS);
		rendering.set(false);
		System.out.println("Render time: "+(System.currentTimeMillis() - timestamp)+"ms");
	}
	
	private Object[] parseTree(KDNode node, Ray ray) {
		if (node.getBoundingBox().intersects(ray) != -1) {
			
			if (node.isLeaf()) {
				double tMin = Double.MAX_VALUE;
				Object3d closest = null;
				for (Object3d obj : node.getObjects()) {
					double t = obj.getIntersection(ray);
					if (t > 0 && t < tMin) {
						tMin = t;
						closest = obj;
					}
				}
				
				return new Object[] {closest, tMin};
			}
			
			boolean leftExists = node.getLeft() != null && !node.getLeft().getObjects().isEmpty();
			boolean rightExists = node.getRight() != null && !node.getRight().getObjects().isEmpty();
			
			Object[] leftNode = new Object[] {null, Double.MAX_VALUE};
			Object[] rightNode =  new Object[] {null, Double.MAX_VALUE};
			
			if (leftExists) {
				leftNode = parseTree(node.getLeft(), ray);
			}
			
			if (rightExists) {
				rightNode = parseTree(node.getRight(), ray);
			}
			
			if (leftNode[0] == null && rightNode[0] == null) {
				return new Object[] {null, Double.MAX_VALUE};
			}
			if (leftNode[0] == null && rightNode[0] != null) {
				return rightNode;
			}
			if (leftNode[0] != null && rightNode[0] == null) {
				return leftNode;
			}
			if (leftNode[0] != null && rightNode[0] != null) {
				if ((double) leftNode[1] < (double) rightNode[1]) {
					return leftNode;
				}
				else {
					return rightNode;
				}
			}
		}
		return new Object[] {null, Double.MAX_VALUE};
	}
	
	private float[] cast(Ray ray, int depth) {
		float[] colors = {0f, 0f, 0f};
		
		Object3d closest = null;
		double tMin = Double.MAX_VALUE;
		
		if (KD_TREE_ENABLED) {
			Object[] ret = parseTree(tree, ray);
			closest = (Object3d) ret[0];
			tMin = (double) ret[1];
		}
		else {
			for (Object3d obj : objects) {
				double t = obj.getIntersection(ray);
				if (t > 0 && t < tMin) {
					tMin = t;
					closest = obj;
				}
			}
		}
		
		// Check if light is closer
		Light closestLight = null;
		double tLight = Double.MAX_VALUE;
		for (Light light : lights) {
			double t = light.getIntersection(ray);
			if (t > 0 && t < tLight && t < tMin) {
				closestLight = light;
				tLight = t;
			}
		}
		
		// If light is closer, color light (no gradient)
		if (closestLight != null) {
			colors[0] = closestLight.getColor().getRed();
			colors[1] = closestLight.getColor().getGreen();
			colors[2] = closestLight.getColor().getBlue();
			return colors;
		}
		
		
		if (closest == null) {
			if (depth > 0) {
				// If we don't do this, the objects tend to develop a shade of the background color.
				return new float[] {0f, 0f, 0f}; 
			}
			else {
				return BACKGROUND_COLOR;
			}
		}
		
		Vec3d intersectionPoint = ray.evaluate(tMin);
		Vec3d normal = null;
		if (closest instanceof Sphere) {
			normal = intersectionPoint.subtract(((Sphere) closest).getCenter()).normalize();
		}
		else if (closest instanceof Triangle) {
			normal = ((Triangle) closest).getFaceNormal(ray);
		}
		for (Light light : lights) {
			Vec3d lightVector = light.getPosition().subtract(intersectionPoint).normalize();
			Color color = closest.getProperties().getColor();
			double dot = lightVector.dotProduct(normal);
			double factor = Math.max(0, dot);
			double ra = KA * color.getRed();
			double ga = KA * color.getGreen();
			double ba = KA * color.getBlue();
			double rd = KD * factor * color.getRed();
			double gd = KD * factor * color.getGreen();
			double bd = KD * factor * color.getBlue();
			if (castShadow(Ray.between(intersectionPoint, light.getPosition()), light)) {
				colors[0] += ra;
				colors[1] += ga;
				colors[2] += ba;
			}
			else {
				colors[0] += ra + rd;
				colors[1] += ga + gd;
				colors[2] += ba + bd;
			}
		}
		if (depth < RECURSIVE_DEPTH) {
			Material material = closest.getProperties().getMaterial();
			if (material == Material.SHINY || material == Material.SHINY_AND_TRANSPARENT) {
				double kr = closest.getProperties().getReflectivity();
				Vec3d reflectDirection = ray.getDirection().subtract(normal.multiply(2 * ray.getDirection().dotProduct(normal)));
				float[] reflectColors = cast(new Ray(intersectionPoint, reflectDirection), depth + 1);
				colors[0] += kr * reflectColors[0];
				colors[1] += kr * reflectColors[1];
				colors[2] += kr * reflectColors[2];
			}
		}
		return colors;
	}
	
	// Returns if there was a shadow
	private boolean castShadow(Ray shadowRay, Light target) {
		
		double tShadow = Double.MAX_VALUE;
		if (KD_TREE_ENABLED) {
			Object[] ret = parseTree(tree, shadowRay);
			tShadow = (double) ret[1];
		}
		else {
			for (Object3d obj : objects) {
				double t = obj.getIntersection(shadowRay);
				if (t > 0 && t < tShadow) {
					tShadow = t;
				}
			}
		}
		
		return tShadow < Double.MAX_VALUE && tShadow < target.getIntersection(shadowRay);
	}
	
	public void close() {
		running.set(false);
	}
	
    protected void paintComponent(Graphics g) {
    	synchronized(image) {
    		g.drawImage(image, 0, 0, null);
        }
    }

}
