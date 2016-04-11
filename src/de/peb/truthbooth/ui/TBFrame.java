package de.peb.truthbooth.ui;

import de.peb.truthbooth.TruthBooth;
import de.peb.truthbooth.UserInformation;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TBFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger("TruthBooth");

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Map<String, Screen> screens;
    private Screen currentScreen;
    public final Dimension screenSize;
    private ActionListener closeDialogListener;
    private JComponent glassPane;
    
    class TBGlassPane extends JComponent {
		private static final long serialVersionUID = 1L;
		private Color grey = new Color(0.5f, 0.5f, 0.5f, 0.5f);
		
		protected void paintComponent(Graphics g) {
    		g.setColor(grey);
    		// TODO: change for something parameterizable
    		g.fillRect(0, 0, 1920, 1080);
    	}
    }
    
    public TBFrame() {
        super("Truth Booth");
        resetUserInformation();
        setUndecorated(true);
        
        glassPane = new TBGlassPane();
        setGlassPane(glassPane);
        glassPane.setOpaque(false);
        glassPane.setVisible(false);

        screens = new HashMap<>();
        addScreen(new LanguageScreen(this));
        
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(screenSize);

        closeDialogListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				hideDialog();
			}
		};

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyDispatcher());
		
//		setAlwaysOnTop(true);
    }
    
    private class KeyDispatcher implements KeyEventDispatcher {

		@Override
		public boolean dispatchKeyEvent(KeyEvent ke) {
			if (ke.getID() == KeyEvent.KEY_PRESSED && ke.getKeyCode() == KeyEvent.VK_F1) {
				try {
					Runtime.getRuntime().exec("gnome-terminal");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}
    }

    public void addScreens() {
    	logger.debug("addScreens");
        addScreen(new IntroScreen(this));
        addScreen(new ThankYouScreen(this));
        addScreen(new RecordingMethodScreen(this));
        addScreen(new RecordVideoScreen(this));
        addScreen(new ArchiveTypeScreen(this));
    }
    
    public void removeScreens() {
    	logger.debug("removeScreens");
    	screens.remove(IntroScreen.SCREEN_ID);
    	screens.remove(ThankYouScreen.SCREEN_ID);
    	screens.remove(RecordingMethodScreen.SCREEN_ID);
    	screens.remove(RecordVideoScreen.SCREEN_ID);
    	screens.remove(ArchiveTypeScreen.SCREEN_ID);
    }
    
    private void addScreen(Screen screen) {
        screens.put(screen.getId(), screen);
    }

    public void resetUserInformation() {
    	logger.debug("resetUserInformation");

        TruthBooth.userInformation = new UserInformation();
    }

    public void show(String screenId) {
    	logger.debug("show screen " + screenId);
    	
    	TruthBooth.resetIdleTimer();
        Screen newScreen = screens.get(screenId);
        if (newScreen != null) {
        	if (currentScreen != null) {
                currentScreen.onHide();
        	}
            setContentPane(newScreen.getComponent());
            currentScreen = newScreen;
            newScreen.onShow();
        }
    }

	public static void centerComponentH(Container comp, Container cont, int height) {
		comp.setLocation((cont.getWidth() - comp.getWidth())/2, height);
	}

	public static void centerComponent(Container c, Container cont) {
		c.setLocation((cont.getWidth() - c.getWidth())/2, (cont.getHeight() - c.getHeight()) / 2);
	}

	public static void centerComponent(Container c, Dimension cont) {
		c.setLocation((cont.width - c.getWidth())/2, (cont.height - c.getHeight()) / 2);
	}
    
    private JDialog currentDialog = null;
    public void hideDialog() {
    	if (currentDialog == null) {
    		return;
    	}
    	currentDialog.setVisible(false);
    	currentDialog = null;
    	glassPane.setVisible(false);
    }
    
    public void showDialog(ImageIcon image, TBButton[] buttons) {
    	for (TBButton btn : buttons) {
    		btn.addActionListener(closeDialogListener);
    	}
    	glassPane.setVisible(true);
    	showDialog(TBDialog.createDialog(this, image, buttons));
    }

    public void showDialog(JDialog dialog) {
    	if (currentDialog != null) {
    		return;
    	}
    	currentDialog = dialog;
    	centerComponent(currentDialog,  screenSize);
    	glassPane.setVisible(true);
    	currentDialog.setVisible(true);
    }
    
    public void updateUIComponents() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (currentScreen != null) {
                    currentScreen.updateUIComponents();
                }
            }
        });
    }
    
    @Override
    public void paintComponents(Graphics g) {
    	super.paintComponents(g);
    	if (currentDialog != null) {
    		Graphics2D g2d = (Graphics2D)g;
    		g2d.setColor(new Color(128,128,128,128));
    		g2d.fillRect(0,  0, screenSize.width, screenSize.height);
    	}
    }

}
