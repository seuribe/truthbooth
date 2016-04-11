package de.peb.truthbooth.ui;

import javax.swing.JComponent;

/**
 * Abstraction for a screen in the application
 * @author Sebastian
 *
 */
public interface Screen {

	public String getId();
	public void onShow();
	public void onHide();
	public void updateUIComponents();
	public JComponent getComponent();
	
}
