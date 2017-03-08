package io.github.dmhacker.rendering.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import io.github.dmhacker.rendering.Constants;
import io.github.dmhacker.rendering.Main;
import io.github.dmhacker.rendering.objects.Light;
import io.github.dmhacker.rendering.objects.Material;
import io.github.dmhacker.rendering.objects.Properties;
import io.github.dmhacker.rendering.objects.Scene;
import io.github.dmhacker.rendering.objects.Sphere;
import io.github.dmhacker.rendering.objects.meshes.Mesh;
import io.github.dmhacker.rendering.objects.meshes.STLObject;
import io.github.dmhacker.rendering.options.Options;
import io.github.dmhacker.rendering.vectors.Vec3d;

public class RenderingFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private RenderingEngine panel;
	
	public RenderingFrame(Options options) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		int side = 1080; // Math.min(screen.width, screen.height) / 2;
		int width = side;
		int height = side;
		
		// int width = screen.width + 4; // So the frame goes to the edge of the screen
		// int height = screen.height - 60; // Minus the bottom programs bar
		// width = Math.min(width, height);
		// height = Math.min(width, height);
		
		System.out.println("Rendering "+width+"x"+height+" image ...");
		
		setSize(width, height);
		setLocation(screen.width / 2 - width / 2, screen.height / 2 - height / 2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("3D Rendering Engine");
		try {
			setIconImage(ImageIO.read(Main.class.getResourceAsStream("icon.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Scene scene = new Scene(new Vec3d(0, 0.25, -3), new Vec3d(0, 1, 0), 0.2);
		scene.add(Light.create(Color.WHITE, new Vec3d(2, 3, -2)));
		
		Properties floorProperties = Properties.create(new Color(222, 184, 135), Material.SHINY).setReflectivity(0.4);
		scene.addFloor(-1, floorProperties);
		
		Map<String, Mesh> meshes = loadMeshes("C:/Users/David Hacker/3D Objects",
				"teapot.stl",
				"StanfordDragon.stl"
		);
		scene.add(meshes.get("teapot.stl")
				.translate(new Vec3d(-0.3, -1, 0.2))
				.setProperties(Properties.create(Color.WHITE, Material.SHINY).setReflectivity(0.2)));
		scene.add(meshes.get("StanfordDragon.stl")
				.translate(new Vec3d(-2, -0.32, 1))
				.setProperties(Properties.create(Color.RED, Material.OPAQUE)));
		scene.add(new Sphere(new Vec3d(-1, -0.8, -0.4), 0.2, Properties.create(Color.CYAN, Material.SHINY).setReflectivity(0.3)));
		
		Class<? extends RenderingEngine> clazz = Options.RENDERING_ENGINES.get(options.getSelectedEngine());
		try {
			Constructor<? extends RenderingEngine> construct = clazz.getConstructor(int.class, int.class, Scene.class, Options.class);
			this.panel = construct.newInstance(width, height, scene, options);
			add(panel);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                panel.close();
                System.exit(0);
            }
		});
		
	}
	
	public void start() {
		panel.start();
		panel.setVisible(true);
		setVisible(true);
	}
	
	public void close() {
		panel.setVisible(false);
		setVisible(false);
		panel.close();
	}
	
	private Map<String, Mesh> loadMeshes(String folderName, String...meshFileNames) {
		Set<String> meshFileNameSet = new HashSet<>();
		meshFileNameSet.addAll(Arrays.asList(meshFileNames));
		Map<String, Mesh> meshMap = new HashMap<>();
		File directory = new File(folderName);
		File[] files = directory.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".stl");
		    }
		});
		for (File file : files) {
			if (meshFileNameSet.size() == 0 || meshFileNameSet.contains(file.getName()))
				meshMap.put(file.getName(), new STLObject(file.getAbsolutePath(), Properties.create(Constants.DEFAULT_OBJECT_COLOR, Constants.DEFAULT_OBJECT_MATERIAL)));
		}
		return meshMap;
	}
}
