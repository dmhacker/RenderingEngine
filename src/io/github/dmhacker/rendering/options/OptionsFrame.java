package io.github.dmhacker.rendering.options;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import io.github.dmhacker.rendering.Main;

public class OptionsFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Options options;
	private JPanel engineSelectionPanel;
	private JPanel engineConfigPanel;

	public OptionsFrame(Options options, OptionsListener listener) {
		this.options = options;
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		int width = screen.width / 2;
		int height = screen.height / 2;

		setSize(width, height);
		setLocation(screen.width / 2 - width / 2, screen.height / 2 - height / 2);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setTitle("3D Rendering Engine - Options");
		try {
			setIconImage(ImageIO.read(Main.class.getResourceAsStream("icon.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1, 3));
		
		ButtonGroup group = new ButtonGroup();
		this.engineSelectionPanel = new JPanel();
		engineSelectionPanel.setLayout(new BoxLayout(engineSelectionPanel, BoxLayout.Y_AXIS));
		mainPanel.add(engineSelectionPanel);
		
		for (String choice : Options.RENDERING_ENGINES.keySet()) {
			JRadioButton choiceButton = new JRadioButton();
			choiceButton.setFont(new Font("Verdana", Font.PLAIN, 18));
			choiceButton.setText(choice);
			choiceButton.setActionCommand(choice);
			choiceButton.addActionListener(this);
			if (choice.equals(options.getSelectedEngine())) {
				choiceButton.setSelected(true);
			}
			group.add(choiceButton);
			engineSelectionPanel.add(choiceButton);
		}
		
		this.engineConfigPanel = new JPanel();
		engineConfigPanel.setLayout(new BoxLayout(engineConfigPanel, BoxLayout.Y_AXIS));
		mainPanel.add(engineConfigPanel);
		
		rebuildConfigurationPanel();
		
		JButton enterButton = new JButton("Start rendering!");
		JFrame frame = this;
		enterButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				listener.onFinish(options);
			}
			
		});
		mainPanel.add(enterButton);
	    
		add(mainPanel);
		
	    setVisible(true);
	}
	
	private void unregisterConfigurationPanel() {
		Map<String, JCheckBox> boxes = options.getSelectedEngineConfiguration();
		for (JCheckBox box : boxes.values()) {
			engineConfigPanel.remove(box);
		}
		validate();
		repaint();
	}
	
	private void rebuildConfigurationPanel() {
		Map<String, JCheckBox> boxes = options.getSelectedEngineConfiguration();
		for (JCheckBox box : boxes.values()) {
			engineConfigPanel.add(box);
		}
		validate();
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		unregisterConfigurationPanel();
		options.setSelectedEngine(event.getActionCommand());
		rebuildConfigurationPanel();
	}
}
