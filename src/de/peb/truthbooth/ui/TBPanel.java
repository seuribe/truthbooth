package de.peb.truthbooth.ui;

import de.peb.truthbooth.TruthBooth;
import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class TBPanel extends JPanel implements Screen {

	public enum Margin {
		Top(100), Bottom(960), Left(360), Right(1560); 

	    private final int value;
		
	    Margin(int value) {
	    	this.value = value;
	    }
	    
	    public static Point corner(Container c, Margin horizontal, Margin vertical) {
	    	return new Point(horizontal.getXFor(c), vertical.getYFor(c));
	    }
	    
	    public int getXFor(Container btn) {
	    	switch (this) {
	    	case Left:
	    		return this.value;
	    	case Right:
	    		return this.value - btn.getWidth();
	    	default:
	    		return 0;
	    	}
	    }

	    public int getYFor(Container btn) {
	    	switch (this) {
	    	case Top:
	    		return this.value;
	    	case Bottom:
	    		return this.value - btn.getHeight();
	    	default:
	    		return 0;
	    	}
	    }

	    public int value() {
	    	return value;
	    }
	}
	
	public static final int TITLE_LINE = 200;

	private static final long serialVersionUID = 1L;

	protected final static Color BACKGROUND_COLOR = Color.white;
	protected TBFrame frame;

	public TBPanel(TBFrame frame) {
		this.frame = frame;
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setBackground(BACKGROUND_COLOR);
		setLayout(null);
	}
	
	public abstract String getId();
	@Override
	public void onShow() {
		//
	}
	
	@Override
	public void onHide() {
		//
	}

	public void setInCorner(Container btn, Margin horizontal, Margin vertical) {
		btn.setLocation(horizontal.getXFor(btn), vertical.getYFor(btn));
	}
	
	public TBButton addNavigationButton(final TBButton button, Point position, final String navigationTarget) {
		button.setLocation(position);
		if (navigationTarget != null) {
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					TruthBooth.showScreen(navigationTarget);
				}
			});
		}
		add(button);
		return button;
	}
	
	public TBButton addQuitButton() {
/*		
		TBButton quitButton = TBButton.getQuitButton();
		quitButton.setLocation(BOTTOM_LEFT_BUTTON_POSITION);
		quitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				TBButton btnResume = TBButton.getStatefullButton("btn_dialog_resume");
				TBButton btnQuit = TBButton.getStatefullButton("btn_dialog_quit");
				TBButton[] buttons = new TBButton[2];
				buttons[0] = btnResume;
				buttons[1] = btnQuit;
				frame.showDialog("Are you sure you want to quit?", buttons);
			}
		});
		return quitButton;
*/
		
		// The quit button makes sure that the file is erased as soon as the user decides to leave
		TBButton btn = TBButton.getQuitButton();
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				TruthBooth.userInformation.deleteTemporaryFile();
			}
		});
		Point p = Margin.corner(btn, Margin.Left, Margin.Bottom);
//		return addNavigationButton(btn, p, LanguageScreen.SCREEN_ID);
		return addNavigationButton(btn, p, TruthBooth.RESTART_ID);	
	}

	public TBButton addBackButton(final String screenId) {
		TBButton btn = TBButton.getBackButton();
		Point p = Margin.corner(btn, Margin.Left, Margin.Top);
		return addNavigationButton(btn, p, screenId);
	}

	public TBButton addNextButton(final String screenId) {
		TBButton btn = TBButton.getNextButton();
		Point p = Margin.corner(btn, Margin.Right, Margin.Bottom);
		return addNavigationButton(btn, p, screenId);
	}

	public TBButton addRestartButton() {
		TBButton btn = TBButton.getRestartButton();
		Point p = Margin.corner(btn, Margin.Right, Margin.Bottom);
		btn.setLocation(p);
		btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				TruthBooth.resetScreens();
				TruthBooth.exit();
			}
		});
		add(btn);
		return btn;
	}

	protected void centerComponent(JComponent c) {
		TBFrame.centerComponent(c,  this);
	}

	protected TBImage addCenteredImage(String baseName) {
		ImageIcon image = TruthBooth.getImageIcon(baseName + ".png");
		TBImage tbImage = new TBImage(image);
		add(tbImage);
		centerComponent(tbImage);
		return tbImage;
	}

	protected TBImage addTitleImage(String baseName) {
		ImageIcon image = TruthBooth.getImageIcon(baseName + ".png");
		TBImage tbImage = new TBImage(image);
		add(tbImage);
//		centerComponent(tbImage);
		tbImage.setLocation((frame.getWidth() - tbImage.getWidth())/2, TITLE_LINE);
		return tbImage;
	}

	@Override
	public void updateUIComponents() {
		//
	}
	
	@Override
	public JComponent getComponent() {
		return this;
	}
}
