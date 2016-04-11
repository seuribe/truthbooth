package de.peb.truthbooth.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import de.peb.truthbooth.TruthBooth;

public class TBButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 1L;
	private ImageIcon imgOn, imgOff, img;

	private static ImageIcon returnImage;
	private static ImageIcon quitImage;
	private static ImageIcon backImage;
	private static ImageIcon nextImage, nextImageOff;


	public TBButton(ImageIcon image) {
		this(image, image, image);
	}

	public TBButton(ImageIcon img, ImageIcon imgOn, ImageIcon imgOff) {
		super(img);
		this.img = img;
		this.imgOn = imgOn == null ? img : imgOn;
		this.imgOff= imgOff == null ? img : imgOff;
		setBackground(Color.WHITE);
		setBorderPainted(false);
		setBorder(null);
		setFocusPainted(false);
		setContentAreaFilled(false);
		setSize(img.getIconWidth(), img.getIconHeight());
		addActionListener(this);
	}

	public TBButton(BufferedImage image) {
		this(new ImageIcon(image));
	}

	public static void clearButtonCache() {
		returnImage = null;
		quitImage = null;
		backImage = null;
		nextImage = nextImageOff = null;
	}
	
	public void setStateOn() {
		super.setIcon(imgOn);
	}
	
	public void setStateOff() {
		super.setIcon(imgOff);
	}

	public void setStateNormal() {
		super.setIcon(img);
	}

	public static TBButton getStatefullButton(String baseName) {
		ImageIcon normal = TruthBooth.getImageIcon(baseName + ".png");
		ImageIcon on;
		ImageIcon off;
		try {
			on = TruthBooth.getImageIcon(baseName + "_on.png");
		} catch (Exception e) {
			on = normal;
		}
		try {
			off = TruthBooth.getImageIcon(baseName + "_off.png");
		} catch (Exception e) {
			off = normal;
		}
		return new TBButton(normal, on, off);
	}
	
	public static TBButton getRestartButton() {
		if (returnImage == null) {
			try {
				returnImage = TruthBooth.getImageIcon("btn_return.png");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new TBButton(returnImage);
	}	

	public static TBButton getQuitButton() {
		if (quitImage == null) {
			try {
				quitImage = TruthBooth.getImageIcon("btn_quit.png");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new TBButton(quitImage);
	}
	
	public static TBButton getBackButton() {
		if (backImage == null) {
			try {
				backImage = TruthBooth.getImageIcon("btn_back.png");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new TBButton(backImage);
	}	
	
	public static TBButton getNextButton() {
		if (nextImage == null) {
			try {
				nextImage = TruthBooth.getImageIcon("btn_next.png");
				nextImageOff = TruthBooth.getImageIcon("btn_next_off.png");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new TBButton(nextImage, nextImage, nextImageOff);
	}
/*
	public TBButton(String text) {
		super(text);
		setBackground(Color.WHITE);
//		setBorderPainted(false);
		setFocusPainted(false);
		setContentAreaFilled(false);
		setSize(dimension);
	}
	*/

	@Override
	public void actionPerformed(ActionEvent arg0) {
		TruthBooth.resetIdleTimer();
	}
}
