package de.peb.truthbooth.test;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;

import junit.framework.TestCase;

public class TestVideoRecorder extends TestCase {

	public void testListWebcams() {
//		WebcamDriver driver = new FFmpegCliDriver();
//		Webcam.setDriver(driver);
		for (Webcam webcam : Webcam.getWebcams()) {
			System.out.println("name: " + webcam.getName());
			WebcamDevice device = webcam.getDevice();
			System.out.println("device: " + device.getName());
			for (Dimension dim : device.getResolutions()) {
				System.out.println("device res: " + dim.toString());
			}
			for (Dimension dim : webcam.getViewSizes()) {
				System.out.println("size: " + dim.toString());
			}
			for (Dimension dim : webcam.getCustomViewSizes()) {
				System.out.println("custom size: " + dim.toString());
			}
		}
	}
	
	public void testWebcamSpeed() {
		
		Dimension[] dims = new Dimension[]{new Dimension(320, 240), new Dimension(640, 480),
				new Dimension(960, 720), new Dimension(1024, 768)};
		
		Webcam webcam = Webcam.getWebcams().get(1);
		webcam.setCustomViewSizes(dims);

		for (Dimension dim : dims) {
			webcam.setViewSize(dim);
			webcam.open();
			System.out.print("dim: " + dim.width + "x" + dim.height);
	
			final int TOTAL_TIME = 10 * 1000;
			long start = System.currentTimeMillis();
			int frames = 0;
			long now = 0;
	
			BufferedImage bi = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_3BYTE_BGR);
			Graphics g = bi.getGraphics();
			
			while (now - start < TOTAL_TIME) {
				BufferedImage image = webcam.getImage();
				g.drawImage(image, 0, 0, null);
				frames++;
//				System.out.println(now + " - " + frames);
				now = System.currentTimeMillis();
			}
			System.out.println(" fps: " + ((float)frames * 1000 / (now - start)));
			webcam.close();
		}
	}
}