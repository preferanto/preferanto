package org.preferanto.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class PreferantoGui extends JFrame {	
	private static final long serialVersionUID = 1L;
	
	public PreferantoGui() {
        super("Preferences");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        PreferantoPanel mainPanel = new PreferantoPanel();
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int width = dim.width * 4 / 5;
		int height = dim.height * 4 / 5;
		mainPanel.setPreferredSize(new java.awt.Dimension(width, height));
		setContentPane(mainPanel);
		pack();
		mainPanel.initDividers();
        setVisible(true);
        mainPanel.setInitialized(true);
	}

	public static void main(final String[] args) throws Exception {
        @SuppressWarnings("unused")
		PreferantoGui viewer = new PreferantoGui();
    }

}
