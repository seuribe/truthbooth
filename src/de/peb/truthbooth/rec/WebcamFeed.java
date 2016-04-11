package de.peb.truthbooth.rec;

import com.github.sarxos.webcam.Webcam;

/**
 * Feeds images from the webcam as soon as they are available
 * @author seu
 *
 */
public class WebcamFeed extends Feed {

	private Webcam webcam;
	
	public WebcamFeed(Webcam webcam) {
		super(20);
		this.webcam = webcam;
		if (!webcam.isOpen()) {
			webcam.open();
		}
	}

	@Override
	protected Object feed() {
		if (webcam.isImageNew()) {
			return new ImageSample(webcam.getImage(), getElapsed());
		}
		return null;
	}
}
