package de.peb.truthbooth.rec;

import java.awt.image.BufferedImage;

public class ImageSample {
	public final BufferedImage image;
	public final long time;
	
	public ImageSample(BufferedImage image, long time) {
		this.image = image;
		this.time = time;
	}
}
