package de.peb.truthbooth.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import de.peb.truthbooth.rec.ImageSample;

public class VideoMonitoringPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;
	private long startTime = 0;
	private BufferedImage image = null;
//	private int frames = 0;
	
	public void setImage(BufferedImage image) {
		this.image = image;
		if (startTime == 0) {
			startTime = System.currentTimeMillis();
		}
//		frames++;
		repaint();
	}

	@Override
	protected void paintComponent(java.awt.Graphics g) {
		Dimension size = getSize();
		g.drawImage(image, 0, 0, size.width, size.height, null);
		g.setColor(Color.black);
//		float fps = (float) (frames * 1000)	/ (System.currentTimeMillis() - startTime);
//		g.drawString("fps: " + fps, 20, 20);
	}

	boolean discard = false;
	@Override
	public void update(Observable obs, Object obj) {
		if (discard = !discard) {
			return;
		}
		if (obj == null || !(obj instanceof ImageSample)) {
			return;
		}
		setImage(((ImageSample)obj).image);
	};
}
