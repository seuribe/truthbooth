package de.peb.truthbooth.ui;

import java.awt.Color;

import javax.swing.JDialog;
import javax.swing.JPanel;

import de.peb.truthbooth.TruthBooth;
import de.peb.truthbooth.UserInformation.Privacy;


public class TBClickWrapDialog extends JPanel {

	private static final long serialVersionUID = 1L;

	public static JDialog createDialog(TBFrame frame, Privacy privacy, TBButton[] buttons) {
		JDialog dialog = new JDialog(frame);
		frame.setBackground(Color.white);
		
		dialog.setUndecorated(true);
		dialog.setBackground(Color.white);
		
		TBClickWrapDialog content = new TBClickWrapDialog(frame, privacy, buttons);
		dialog.setSize(content.getWidth(), content.getHeight());
		dialog.setContentPane(content);
		return dialog;
	}

	public TBClickWrapDialog(final TBFrame frame, Privacy privacy, TBButton[] buttons) {
		setLayout(null);

		String imageName = "text_terms_" + privacy.toString().toLowerCase() + ".png";
		TBImage terms = new TBImage(TruthBooth.getImageIcon(imageName));
		int width = terms.getWidth();
		int height = terms.getHeight();

		setBounds(0, 0, width + 20, height + 100);

		add(terms);
		terms.setBounds(10, 10, width, height);		
		
		int x = width - 10;
		int y = height + 20;
		for (int i = buttons.length-1 ; i >= 0 ; i--) {
			TBButton btn = buttons[i];
			x -= btn.getWidth();
			btn.setBounds(x,  y, btn.getWidth(), btn.getHeight());
			x -= 10;
			add(btn);
		}

		setBackground(Color.white);
	}
	
}
