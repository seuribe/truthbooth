package de.peb.truthbooth.ui;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class TBImage extends JLabel {
	private static final long serialVersionUID = 1L;

	public TBImage(BufferedImage image) {
		this(new ImageIcon(image));
	}

	public TBImage(ImageIcon image) {
		super(image);
		setSize(image.getIconWidth(), image.getIconHeight());
	}
}
