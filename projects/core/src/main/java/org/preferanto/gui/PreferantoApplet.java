package org.preferanto.gui;

import javax.swing.JApplet;

public class PreferantoApplet extends JApplet {	
	private static final long serialVersionUID = 1L;
	
	public PreferantoApplet() {        
	}
	
	@Override
	public void init() {
		super.init();

		PreferantoPanel mainPanel = new PreferantoPanel();
		
        setContentPane(mainPanel);
        validate();
		mainPanel.initDividers();
        setVisible(true);
        mainPanel.setInitialized(true);
	}
}
