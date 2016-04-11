package de.peb.truthbooth.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import de.peb.truthbooth.rec.AudioSample;

public class ProgressBar extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;

	private BufferedImage image;
	private Graphics2D g2d;
	private int height;
	private int width;
	private double samplesPerPixel;
	private double pixPerMillis;

	private double oldx = 0;
	private double oldavg = 0;

	private final static Color PROGRESS_COLOR = new Color(33, 255, 112);
	private final static Color BACK_CENTER_COLOR = new Color(247, 247, 247);
	private final static Color BACK_COLOR = new Color(255, 255, 255);

	public ProgressBar(int width, int height, long time, int samplesPerSecond) {
		this.width = width;
		this.height = height;
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.pixPerMillis = (double) width / (time * 1000);
		this.samplesPerPixel = (double)(time * samplesPerSecond) / width;
		this.g2d = (Graphics2D)image.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		resetImage();
	}

	
	@Override
	public void update(Observable obs, Object obj) {
		if (obj instanceof AudioSample) {
			AudioSample sample = (AudioSample)obj;
			double timeInMillis = sample.position / 1000000;
			double x = (timeInMillis * pixPerMillis);
			double mid = height/2;

			int chunks = (int) (sample.buffer.length / samplesPerPixel);
			int start = 0;
			if (chunks == 0) {
				double avg = getAverage(sample.buffer, start, sample.buffer.length) * 5;
				g2d.drawLine((int)x, (int)(mid-(avg/2)), (int)x, (int)(mid+(avg/2)));
			}
			for (int i = 0 ; i < chunks ; i++, start += samplesPerPixel) {
				int w = (int) samplesPerPixel;
				if (w + start >= sample.buffer.length) {
					w = sample.buffer.length - start - 1;
				}
				double avg = getAverage(sample.buffer, start, w) * 5;
				if (avg > height) {
					avg = height;
				}
				double oldy1 = (int) (mid - (oldavg/2));
				double oldy2 = (int) (mid + (oldavg/2));
				double y1 = mid - (avg/2);
				double y2 = mid + (avg/2);

				Polygon poly = new Polygon();
				poly.addPoint((int)oldx-1, (int)oldy1-1);
				poly.addPoint((int)x+1,  (int)y1-1);
				poly.addPoint((int)x+1,  (int)y2+1);
				poly.addPoint((int)oldx-1,  (int)oldy2+1);

				g2d.fillPolygon(poly);
				g2d.drawPolygon(poly);
				
				oldavg = avg;
				oldx = x;
				x += (w / samplesPerPixel);
			}
			oldx = x;
		}
		repaint();
	}

	public int getAverage(short[] buffer, int start, int n) {
		double sum = 0;
		for (int i = start ; i < start + n ; i++) {
			short s = buffer[i];
			sum += (s < 0) ? -s : s;
		}
		double level = (float)sum / buffer.length;
		return (int)((level * 100) / Short.MAX_VALUE);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image,  0,  0,  null);
	}

	public void resetImage() {
		g2d.setColor(BACK_COLOR);
		g2d.fillRect(0, 0, width, height);
		g2d.setColor(BACK_CENTER_COLOR);
		g2d.fillRect(0, height/4,  width, height/2);
		g2d.setColor(PROGRESS_COLOR);
		g2d.setStroke(new BasicStroke(1.5f));
		oldx = 0;
		oldavg = 0;
	}

}
