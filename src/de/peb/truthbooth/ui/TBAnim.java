package de.peb.truthbooth.ui;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import de.peb.truthbooth.TruthBooth;

public class TBAnim extends JLabel {

	private static final long serialVersionUID = 1L;

	private final ImageIcon[] frames;
	private final long delay;
	private TimerTask updateTask;
	private Timer timer;
	private int currentFrame;
	
	/**
	 * Format for image files is [baseName]-[numFrame].png<br>
	 * example: anim_clock-1.png, anim_clock-2.png, etc.
	 * 
	 * @param baseName  base name for image files to load
	 * @param numFrames number of frames in animation
	 * @param delay		delay between frames
	 */
	public TBAnim(String baseName, int numFrames, long delay) {
		frames = new ImageIcon[numFrames];
		for (int i = 0 ; i < numFrames ; i++) {
			frames[i] = TruthBooth.getImageIcon(baseName + "-" + (i+1) + ".png");
		}
		this.delay = delay;
		currentFrame = 0;
		switchImage();
	}
	
	private void switchImage() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setIcon(frames[currentFrame]);
				currentFrame = (currentFrame + 1) % frames.length;
			}
		});
	}
	
	public void start() {
		if (timer != null) {
			return;
		}
		timer = new Timer();
		updateTask = new TimerTask() {
			@Override
			public void run() {
				switchImage();
			}
		};
		timer.scheduleAtFixedRate(updateTask, delay, delay);
	}
	
	public void stop() {
		if (timer == null) {
			return;
		}
		timer.cancel();
		timer = null;
		currentFrame = 0;
	}
	
}
