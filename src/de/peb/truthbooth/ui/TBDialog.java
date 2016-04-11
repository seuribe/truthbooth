package de.peb.truthbooth.ui;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TBDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int WIDTH  = 581;
	private static final int HEIGHT = 319;
	
	public static JDialog createDialog(TBFrame frame, ImageIcon image, TBButton[] buttons) {
		JDialog dialog = new JDialog(frame);
		frame.setBackground(Color.white);
		dialog.setSize(WIDTH, HEIGHT);
		
		dialog.setUndecorated(true);
		dialog.setBackground(Color.white);
		
		TBDialog content = new TBDialog(image, buttons);
		dialog.setContentPane(content);
		return dialog;
	}

	private TBDialog(ImageIcon image, TBButton[] buttons) {
		setLayout(null);
		setSize(WIDTH, HEIGHT);
/*
		Font font = new Font("Century Gothic", Font.PLAIN, 18);
		setBackground(Color.white);

		JLabel label = new JLabel(text);
		label.setFont(font);
		label.setBackground(Color.white);
		label.setForeground(new Color(88, 89, 91));
		label.setBounds(20, 20, WIDTH - 40, HEIGHT - 80);
		add(label);
*/
		int x = WIDTH - 10;
		int y = 230;
		for (int i = buttons.length-1 ; i >= 0 ; i--) {
			TBButton btn = buttons[i];
			x -= btn.getWidth();
			btn.setBounds(x,  y, btn.getWidth(), btn.getHeight());
			x -= 10;
			add(btn);
		}

		JLabel backImage = new JLabel();
		backImage.setIcon(image);
		add(backImage);
		backImage.setBounds(0, 0, image.getIconWidth(), image.getIconHeight());
		setBackground(Color.white);
	}
	
}
