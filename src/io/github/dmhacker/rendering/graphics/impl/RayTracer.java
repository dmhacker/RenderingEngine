package io.github.dmhacker.rendering.graphics.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import io.github.dmhacker.rendering.graphics.RenderingEngine;
import io.github.dmhacker.rendering.kdtrees.KDNode;
import io.github.dmhacker.rendering.objects.Light;
import io.github.dmhacker.rendering.objects.Material;
import io.github.dmhacker.rendering.objects.Object3d;
import io.github.dmhacker.rendering.objects.Properties;
import io.github.dmhacker.rendering.objects.Sphere;
import io.github.dmhacker.rendering.objects.Triangle;
import io.github.dmhacker.rendering.objects.meshes.GenericMesh;
import io.github.dmhacker.rendering.objects.meshes.Mesh;
import io.github.dmhacker.rendering.objects.meshes.STLObject;
import io.github.dmhacker.rendering.vectors.Quaternion;
import io.github.dmhacker.rendering.vectors.Ray;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class RayTracer extends RenderingEngine {
	private static final long serialVersionUID = 1L;
	
	//================================================================================
	// Rendering Options
	//================================================================================
	private static final int THREADS = Runtime.getRuntime().availableProcessors();
	private static final double ZOOM = 2;
	
	//================================================================================
	// Lighting Properties
	//================================================================================
	private static final float[] BACKGROUND = toColorArray(Color.BLACK);
	
	private static final double LIGHT_AMBIENCE = 0.1;
	private static final double LIGHT_ATTENUATION_MEDIUM = 1.64;
	
	private static final int SHADOW_SAMPLES = 6;
	
	private static final int RECURSIVE_DEPTH = 9;
	
	private static final double TRANSPARENCY_SCALE = 0.05;
	
	//================================================================================
	// Anti-aliasing Properties
	//================================================================================
	private static final int ANTIALIASING_SAMPLE_SUBDIVISIONS = 3;
	private static final double GAUSSIAN_RMS_WIDTH = 1; // A very high width will act like a box filter
	private static final double GAUSSIAN_MEAN = (ANTIALIASING_SAMPLE_SUBDIVISIONS - 1) / 2.0;
	private static final double GAUSSIAN_C_INV = 1.0 / (2.0 * GAUSSIAN_RMS_WIDTH * GAUSSIAN_RMS_WIDTH);
	private static final double GAUSSIAN_SCALE;
	static {
		double total = 0;
		for (int i = 0; i < ANTIALIASING_SAMPLE_SUBDIVISIONS; i++) {
			for (int j = 0; j < ANTIALIASING_SAMPLE_SUBDIVISIONS; j++) {
				total += gaussian(i - GAUSSIAN_MEAN, j - GAUSSIAN_MEAN);
			}
		}
		GAUSSIAN_SCALE = 1.0 / total;
	}
	
	//================================================================================
	// Variables
	//================================================================================
	private AtomicBoolean running;
	private AtomicBoolean rendering;
	
	private BufferedImage image;
	private final int width;
	private final int height;
	private final int area;
	
	private Vec3d camera;
	private Quaternion cameraRotation;
	private List<Object3d> objects;
	private List<Light> lights;
	private Map<Vec3d, Integer> edgePixels;
	
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
				else if (e.getKeyChar() == 'w') {
					if (rendering.get() || !RayTracerOption.WIREFRAME.get())
						return;
					for (Entry<Vec3d, Integer> entry : edgePixels.entrySet()) {
						int px = (int) entry.getKey().getX();
						int py = (int) entry.getKey().getY();
						int oldRGB = image.getRGB(px, py);
						image.setRGB(px, py, entry.getValue());
						edgePixels.put(entry.getKey(), oldRGB);
					}
					repaint();
				}
			}
			
		});
		
		this.camera = new Vec3d(0, 0.25, -1);
		this.cameraRotation = new Quaternion(0, 0, 0, 0);
		
		this.lights = new ArrayList<>();
		lights.add(new Light(Color.WHITE, new Vec3d(-1, 3, -2), 0.9, 1.0, 500));
		// lights.add(new Light(Color.WHITE, new Vec3d(-5, 2, 8), 0.9, 1.0, 500));
		// lights.add(new Light(Color.WHITE, new Vec3d(3, 2.5, 7), 0.9, 1.0, 500));
		
		this.objects = new ArrayList<>();
		this.edgePixels = new ConcurrentHashMap<>();
		
		addFloor(-1);

		/*
		Properties mirror = new Properties(new Color(196, 199, 206), Material.SHINY, 0.7, 1);
		Vec3d tl = new Vec3d(-2, 1, 6);
		Vec3d tr = new Vec3d(2, 1, 6);
		Vec3d bl = new Vec3d(-2, -1, 6);
		Vec3d br = new Vec3d(2, -1, 6);
		Triangle t1 = new Triangle(null, bl, tl, tr, mirror);
		Triangle t2 = new Triangle(null, bl, br, tr, mirror);
		List<Triangle> ts = new ArrayList<Triangle>();
		ts.add(t1);
		ts.add(t2);
		addMesh(new GenericMesh(ts));
		*/
		
		// Mesh letterW = new TextSTLObject("C:/Users/David Hacker/3D Objects/Alphabet/Letter_W.stl", new Vec3d(-2, -1, 2), 1.4, false, new Properties(new Color(255, 167, 38), Material.OPAQUE, 0.2, 1.0));
		// Mesh letterH = new TextSTLObject("C:/Users/David Hacker/3D Objects/Alphabet/Letter_H.stl", new Vec3d(-0.5, -1, 2), 1, false, new Properties(new Color(1, 87, 155), Material.OPAQUE, 0.2, 1.0));
		// Mesh letterS = new TextSTLObject("C:/Users/David Hacker/3D Objects/Alphabet/Letter_S.stl", new Vec3d(0.8, -1, 2), 0.9, false, new Properties(new Color(255, 167, 38), Material.OPAQUE, 0.2, 1.0));
		
		// Mesh drinkingCup = new BinarySTLObject("C:/Users/David Hacker/3D Objects/DrinkingCup.stl", new Vec3d(-0.5, -1.0, 1.5), false, new Properties(new Color(255, 255, 255), Material.OPAQUE, 0.4, 1));
		Mesh teapot = new STLObject("C:/Users/David Hacker/3D Objects/teapot.stl", new Vec3d(0, -1, 2.1), 1, new Properties(Color.YELLOW, Material.SHINY, 0.2, 1));
		// Mesh tieFront = new BinarySTLObject("C:/Users/David Hacker/3D Objects/TIE-front.stl", new Vec3d(0, -1.0, 1.3), 1, new Properties(new Color(255, 189, 23), Material.OPAQUE, 0.5, 1));
		// Mesh torusKnot = new TextSTLObject("C:/Users/David Hacker/3D Objects/TripleTorus.stl", new Vec3d(-1, -0.5, 3), 1, new Properties(Color.PINK, Material.SHINY, 0.2, 1.0));
		// Mesh stanfordBunny = new STLObject("C:/Users/David Hacker/3D Objects/StanfordBunny.stl", new Vec3d(1.3, -1.025, 1.3), 0.8, new Properties(new Color(140, 21, 21), Material.OPAQUE, 0.3, 1));
		// Mesh stanfordDragon = new STLObject("C:/Users/David Hacker/3D Objects/StanfordDragon.stl", new Vec3d(-1.2, -0.5, 1.2), 0.75, new Properties(new Color(140, 21, 21), Material.OPAQUE, 0.3, 1));
		// Mesh mandelbulb = new BinarySTLObject("C:/Users/David Hacker/3D Objects/mandelbulb.stl", new Vec3d(-0.2, -1, 3), false, new Properties(new Color(140, 21, 21), Material.OPAQUE, 0.3, 1));
		// Mesh skull = new BinarySTLObject("C:/Users/David Hacker/3D Objects/Skull.stl", new Vec3d(-0.5, -1, 2.7), 0.9, new Properties(Color.WHITE, Material.OPAQUE, 0.2, 1));
		// Mesh halfDonut = new STLObject("C:/Users/David Hacker/3D Objects/HalfDonut.stl", new Vec3d(0.5, 0, 1), -1, new Properties(new Color(255, 189, 23), Material.SHINY, 0.3, 1.0));
		// Mesh gravestone = new STLObject("C:/Users/David Hacker/3D Objects/Gravestone.stl", new Vec3d(0, -1.05, 2), 1, new Properties(new Color(255, 255, 255), Material.OPAQUE, 0.3, 1.0));
		
		// addMesh(letterW);
		// addMesh(letterH);
		// addMesh(letterS);
		// addMesh(drinkingCup);
		addMesh(teapot);
		// addMesh(torusKnot);
		// addMesh(skull);
		// addMesh(stanfordDragon);
		// addMesh(stanfordBunny);
		// addMesh(mandelbulb);
		// addMesh(tieFront);
		// addMesh(gravestone);
		
		// objects.add(new Sphere(new Vec3d(0, -0.5, 5), 0.5, new Properties(Color.GREEN, Material.SHINY, 0.4, 1)));
		// objects.add(new Sphere(new Vec3d(-0.05, -0.9, 1.8), 0.1, new Properties(Color.MAGENTA, Material.SHINY, 0.4, 1)));
		// objects.add(new Sphere(new Vec3d(0.35, -0.8, 1.6), 0.2, new Properties(Color.CYAN, Material.SHINY, 0.4, 1)));
		
		Collection<JCheckBox> checkBoxes = RayTracerOption.getCheckboxes();
		Object[] content = new Object[checkBoxes.size() + 1];
		content[0] = "Options for ray tracing";
		int counter = 0;
		for (JCheckBox cb : checkBoxes) {
			content[++counter] = cb;
		}

		int n =  JOptionPane.showConfirmDialog(this, content,  "Render "+objects.size()+" objects?", JOptionPane.YES_NO_OPTION); 
		if (n != 0) {
			System.exit(0);
		}

		if (RayTracerOption.KD_TREE.get()) {
			long timestamp = System.currentTimeMillis();
			this.tree = KDNode.build(null, objects, 0);
			long generationTime = System.currentTimeMillis() - timestamp;
			System.out.println("kd-tree generation: "+generationTime+"ms ("+Math.round(generationTime / 1000.0)+"s)");
		}	
	}
	
	public void addFloor(double surfaceY) {
		Vec3d topLeft = new Vec3d(-10000, surfaceY, 10000);
		Vec3d bottomLeft = new Vec3d(-10000, surfaceY, -10000);
		Vec3d topRight = new Vec3d(10000, surfaceY, 10000);
		Vec3d bottomRight = new Vec3d(10000, surfaceY, -10000);

		Properties floorProperties = new Properties(new Color(130, 82, 1), Material.SHINY, 0.4, 1);
		Triangle topLeftPortion = new Triangle(null, bottomLeft, topLeft, topRight, floorProperties);
		Triangle bottomRightPortion = new Triangle(null, bottomLeft, bottomRight, topRight, floorProperties);
		
		List<Triangle> floorTriangles = new ArrayList<Triangle>();
		floorTriangles.add(topLeftPortion);
		floorTriangles.add(bottomRightPortion);
		GenericMesh floorMesh = new GenericMesh(floorTriangles);
		
		addMesh(floorMesh);
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
		final double scale = 1.0 / ZOOM / Math.min(width, height);
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
									point = new Vec3d(nx + Math.random() * sampleLength, ny + Math.random() * sampleLength, 0).rotate(cameraRotation);
									float[] sample = cast(Ray.between(camera, point), px, py);
									for (int k = 0; k < 3; k++)
										rawColor[k] += gaussian(i - GAUSSIAN_MEAN, j - GAUSSIAN_MEAN) * sample[k];
								}
							}
							for (int i = 0; i < 3; i++)
								rawColor[i] *= GAUSSIAN_SCALE;
						}
						else {
							Vec3d point;
							point = new Vec3d(x, y, 0).rotate(cameraRotation);
							rawColor = cast(Ray.between(camera, point), px, py);
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
	}
	
	private Object[] parseTree(KDNode node, Ray ray, boolean shadow) {
		if (node.getBoundingBox().isIntersecting(ray)) {
			
			if (node.isLeaf()) {
				double tMin = Double.MAX_VALUE;
				Object3d closest = null;
				for (Object3d obj : node.getObjects()) {
					if (shadow && obj.isTransparent()) {
						continue;
					}
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
				leftNode = parseTree(node.getLeft(), ray, shadow);
			}
			
			if (rightExists) {
				rightNode = parseTree(node.getRight(), ray, shadow);
			}
			
			if (leftNode[0] == null && rightNode[0] == null) {
				return leftNode; // Could be either
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
	
	private float[] cast(Ray ray, int pixelWidth, int pixelHeight) {
		return cast(ray, 0, pixelWidth, pixelHeight);
	}
	
	private float[] cast(Ray ray, int depth, int pixelWidth, int pixelHeight) {
		float[] colors = {0f, 0f, 0f};
		
		Object3d closest = null;
		double tMin = Double.MAX_VALUE;
		
		if (RayTracerOption.KD_TREE.get()) {
			Object[] ret = parseTree(tree, ray, false);
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
		
		if (RayTracerOption.VIEW_LIGHT_SOURCES.get()) {
			Light hitLight = null;
			for (Light light : lights) {
				double tlight = light.getIntersection(ray);
				if (tlight > 0 && tlight < tMin) {
					tMin = tlight;
					hitLight = light;
				}
			}
			if (hitLight != null) {
				Color lightColor = hitLight.getColor();
				return new float[] {
					lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), 
				};
			}
		}
		
		if (closest == null) {
			return BACKGROUND;
		}
		
		Vec3d intersectionPoint = ray.evaluate(tMin);
		
		if (RayTracerOption.WIREFRAME.get() && depth == 0 && closest instanceof Triangle) {
			Triangle triangle = (Triangle) closest;
			for (int i = 0; i < 3; i++) {
				Vec3d v1 = triangle.getVertices().get(i);
				Vec3d v2 = triangle.getVertices().get((i + 1) % 3);
				double dist = distanceToSegment(intersectionPoint, v1, v2);
				if (dist < 0.0015 / ZOOM) {
					edgePixels.put(new Vec3d(pixelWidth, pixelHeight, 0), Color.WHITE.getRGB());
					break;
				}
			}
		}
		
		Vec3d normal = null;
		if (closest instanceof Sphere) {
			normal = intersectionPoint.subtract(((Sphere) closest).getCenter()).normalize();
		}
		else if (closest instanceof Triangle) {
			normal = ((Triangle) closest).getNormal(ray, intersectionPoint);
		}
		for (Light light : lights) {
			Vec3d unnormalizedLightVector = light.getPosition().subtract(intersectionPoint);
			Vec3d lightVector = unnormalizedLightVector.normalize();
			Vec3d halfwayVector = lightVector.subtract(ray.getDirection()).normalize();
			Color color = multiplyColors(closest.getProperties().getColor(), light.getColor());
			double diffuseIntensity = Math.max(0, lightVector.dotProduct(normal));
			double specularIntensity = Math.pow(Math.max(0, halfwayVector.dotProduct(normal)), light.getSpecularHardness());
			double ra = LIGHT_AMBIENCE * color.getRed();
			double ga = LIGHT_AMBIENCE * color.getGreen();
			double ba = LIGHT_AMBIENCE * color.getBlue();
			double rd = light.getDiffusePower() * diffuseIntensity * color.getRed();
			double gd = light.getDiffusePower() * diffuseIntensity * color.getGreen();
			double bd = light.getDiffusePower() * diffuseIntensity * color.getBlue();
			double rs = light.getSpecularPower() * specularIntensity * color.getRed();
			double gs = light.getSpecularPower() * specularIntensity * color.getGreen();
			double bs = light.getSpecularPower() * specularIntensity * color.getBlue();
			double falloffScale = 1.0 / Math.pow(1 + unnormalizedLightVector.distance() / (light.getRadius() * LIGHT_ATTENUATION_MEDIUM * 100), 2);
			double shadowScale = 0;
			if (RayTracerOption.SOFT_SHADOWS.get()) {
				for (int i = 0; i < SHADOW_SAMPLES; i++) {
					double maxDistance = light.getRadius() / 2 * Math.sqrt(2);
					double xVariance = Math.random() * maxDistance - maxDistance / 2;
					double yVariance = Math.random() * maxDistance - maxDistance / 2;
					double zVariance = Math.random() * maxDistance - maxDistance / 2;
					if (castShadow(Ray.between(intersectionPoint, light.getPosition().add(xVariance, yVariance, zVariance)), light)) {
						shadowScale += 1;
					}
				}
				shadowScale /= SHADOW_SAMPLES;
				shadowScale = 1.0 - shadowScale;
			}
			else {
				shadowScale = 1;
				if (castShadow(Ray.between(intersectionPoint, light.getPosition()), light)) {
					shadowScale = 0;
				}
			}
			Material material = closest.getProperties().getMaterial();
			double[] additions = new double[] {
					falloffScale * (ra + shadowScale * (rd + rs)),
					falloffScale * (ga + shadowScale * (gd + gs)),
					falloffScale * (ba + shadowScale * (bd + bs))
			};
			
			if (material == Material.TRANSPARENT || material == Material.SHINY_AND_TRANSPARENT) {
				for (int i = 0; i < 3; i++)
					additions[i] *= TRANSPARENCY_SCALE;
			}
			for (int i = 0; i < 3; i++)
				colors[i] += additions[i];
			if (depth < RECURSIVE_DEPTH) {
				if (material == Material.SHINY || material == Material.SHINY_AND_TRANSPARENT) {
					double kr = closest.getProperties().getReflectivity();
					Vec3d reflectDirection = ray.getDirection().subtract(normal.multiply(2 * ray.getDirection().dotProduct(normal)));
					float[] reflectColors = cast(new Ray(intersectionPoint, reflectDirection), depth + 1, pixelWidth, pixelHeight);
					colors[0] += falloffScale * kr * reflectColors[0];
					colors[1] += falloffScale * kr * reflectColors[1];
					colors[2] += falloffScale * kr * reflectColors[2];
				}
				if (closest.isTransparent()) {
					Vec3d transmitDirection = ray.getDirection();
					float[] transmittedColors = cast(new Ray(intersectionPoint.add(transmitDirection.multiply(0.000001)), transmitDirection), depth + 1, pixelWidth, pixelHeight);
					colors[0] += falloffScale * transmittedColors[0];
					colors[1] += falloffScale * transmittedColors[1];
					colors[2] += falloffScale * transmittedColors[2];
				}
			}
		}
		return colors;
	}
	
	// Returns if there was a shadow
	private boolean castShadow(Ray shadowRay, Light target) {
		
		double tShadow = Double.MAX_VALUE;
		if (RayTracerOption.KD_TREE.get()) {
			Object[] ret = parseTree(tree, shadowRay, true);
			tShadow = (double) ret[1];
		}
		else {
			for (Object3d obj : objects) {
				if (obj.isTransparent()) {
					continue;
				}
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

	// Two dimensional gaussian function according to
	// http://homepages.inf.ed.ac.uk/rbf/HIPR2/gsmooth.htm
	// Removed the A parameter [sqrt(1 / (2 * pi * c^2)] because curve is scaled at the end
	private static double gaussian(double i, double j) {
		return Math.exp(-(i * i + j * j) * GAUSSIAN_C_INV);
	}
	
	private static Color multiplyColors(Color color1, Color color2) {
		float r1 = color1.getRed() / 255.0f;
		float g1 = color1.getGreen() / 255.0f;
		float b1 = color1.getBlue() / 255.0f;
		float a1 = color1.getAlpha() / 255.0f;

		float r2 = color2.getRed() / 255.0f;
		float g2 = color2.getGreen() / 255.0f;
		float b2 = color2.getBlue() / 255.0f;
		float a2 = color2.getAlpha() / 255.0f;
		
		float r3 = r1 * r2;
		float g3 = g1 * g2;
		float b3 = b1 * b2;
		float a3 = a1 * a2;
		
		Color color3 = new Color((int) (r3 * 255), (int) (g3 * 255), (int) (b3 * 255), (int) (a3 * 255));
		return color3;
	}

	private static float[] toColorArray(Color color) {
		return new float[] {
				color.getRed(),
				color.getGreen(),
				color.getBlue()
		};
	} 
	
	public static double distanceToSegment(Vec3d v, Vec3d a, Vec3d b) {
		Vec3d ab = b.subtract(a);
		Vec3d av = v.subtract(a);

		if (av.dotProduct(ab) <= 0.0)       
			return av.distance();        

		Vec3d bv  = v.subtract(b) ;

		if (bv.dotProduct(ab) >= 0.0)   
			return bv.distance(); 

		return (ab.cross(av)).distance() / ab.distance() ;     
	}
}
