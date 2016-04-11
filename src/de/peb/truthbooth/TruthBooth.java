package de.peb.truthbooth;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.peb.truthbooth.ui.ConfigPanel;
import de.peb.truthbooth.ui.IntroScreen;
import de.peb.truthbooth.ui.LanguageScreen;
import de.peb.truthbooth.ui.TBButton;

import de.peb.truthbooth.ui.TBFrame;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class TruthBooth {

	public enum ResetTimer {
		None,
		Normal,
		SaveDialog;
		
		protected void Set() {
			switch(this) {
				case None:
					currentTimer = NO_TIMEOUT;
					break;
				case Normal:
					currentTimer = IDLE_TIMEOUT;
					break;
				case SaveDialog:
					currentTimer = SAVEDIALOG_TIMEOUT;
					break;
				}
		}
	}
	
	public static final String RESTART_ID 	= "RESTART";
	
	private static final String CONFIG_FILE = "truthbooth.config";

    public static int VIDEO_WIDTH 			= 1280;
    public static int VIDEO_HEIGHT 			= 720;
	public static double VIDEO_FPS 			= 25;

	public static int RECORD_SECONDS 		= 150;
	public static int RECORD_TIME_MS 		= 150 * 1000; // time in seconds

	public static String FILE_EXTENSION 	= ".mp4";
    public static int VIDEO_PREVIEW_WIDTH 	= VIDEO_WIDTH / 4;
    public static int VIDEO_PREVIEW_HEIGHT 	= VIDEO_HEIGHT / 4;

    public static String[] LANGUAGES = {};
    public static int BLINK_DELAY_MS = 500;
    
    public static Webcam webcam;
    public static TargetDataLine line;
    public static UserInformation userInformation;

    public static final int AUDIO_BUFFER_SIZE 	= 4096;
    public static int AUDIO_RATE 				= 22050;
    public static int AUDIO_BITS 				= 16;
    public static int AUDIO_CHANNELS 			= 1;
    public static final boolean BIG_ENDIAN = true;
    public static final boolean SIGNED = true;
    
    public static String DEFAULT_AUDIO_DEVICE = null;
    public static String DEFAULT_VIDEO_DEVICE = null;
    
    /**
     * Timeout in seconds to reset program if there is no input from user
     */
    private static int NO_TIMEOUT = -1;
    private static int IDLE_TIMEOUT = 300;
    private static int SAVEDIALOG_TIMEOUT = 20;

    private static int currentTimer;
    
    public final static AudioFormat audioFormat = new AudioFormat(AUDIO_RATE, AUDIO_BITS, AUDIO_CHANNELS, SIGNED, BIG_ENDIAN);
    public final static DataLine.Info recordLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
    
    private static TBFrame frame;
    
    private static final Logger logger = LoggerFactory.getLogger("TruthBooth");

    public static ImageIcon getImageIcon(String filename) {
    	return getImageIcon(userInformation.getLanguage(), filename);
    }

    public static ImageIcon getImageIcon(String lang, String filename) {
    	String resource = "/images/" + lang  + "/" + filename;
    	logger.debug("Reading image icon " + resource);
    	URL resURL = TruthBooth.class.getResource(resource);
    	return new javax.swing.ImageIcon(resURL);
    }

    public static void showScreen(String screenId) {
    	logger.info("Showing screen " + screenId);
    	if (screenId == RESTART_ID) {
    		exit();
    	} else {
            frame.show(screenId);
    	}
    }

    
    public static void main(String args[]) {
    	logger.info("Main start");

    	readConfig();
        frame = new TBFrame();
        frame.resetUserInformation();
        DeviceSelector ds = new DeviceSelector(DEFAULT_VIDEO_DEVICE, DEFAULT_AUDIO_DEVICE);
        if (ds.checkAndSetDefaults()) {
            frame.setVisible(true);
        	logger.info("Default devices found, starting without device selection");
            showScreen(LanguageScreen.SCREEN_ID);
            resetIdleTimer();
        } else {
        	logger.info("Default devices NOT found, showing device selection");
            ConfigPanel configPanel = new ConfigPanel(ds);
            frame.setContentPane(configPanel);
            frame.setVisible(true);
        }
    }
    
    private static double getDoubleValue(Properties p, String key, double defaultValue) {
    	try {
    		return Double.parseDouble(p.getProperty(key));
    	} catch (NumberFormatException nfe) {
    		return defaultValue;
    	}
    }

    private static int getIntValue(Properties p, String key, int defaultValue) {
    	try {
    		return Integer.parseInt(p.getProperty(key));
    	} catch (NumberFormatException nfe) {
    		return defaultValue;
    	}
    }
    
    private static void readConfig() {
    	Properties p = new Properties();
    	try {
        	logger.info("reading config file " + CONFIG_FILE);
			p.load(new FileInputStream(CONFIG_FILE));

			FILE_EXTENSION = p.getProperty("file.extension");

			VIDEO_WIDTH 	= getIntValue(p, "video.width", VIDEO_WIDTH);
			VIDEO_HEIGHT 	= getIntValue(p, "video.height", VIDEO_HEIGHT);
			VIDEO_FPS 		= getDoubleValue(p, "video.fps", VIDEO_FPS);
			RECORD_TIME_MS 	= getIntValue(p, "record.time", RECORD_SECONDS) * 1000;
			
			AUDIO_RATE 		= getIntValue(p, "audio.rate", AUDIO_RATE);
			AUDIO_BITS 		= getIntValue(p, "audio.bits", AUDIO_BITS);
			AUDIO_CHANNELS 	= getIntValue(p, "audio.channels", AUDIO_CHANNELS);
			
			DEFAULT_AUDIO_DEVICE = p.getProperty("audio.device.default");
			DEFAULT_VIDEO_DEVICE = p.getProperty("video.device.default");
			
			IDLE_TIMEOUT 		= getIntValue(p, "idle.timeout", IDLE_TIMEOUT);
			SAVEDIALOG_TIMEOUT 	= getIntValue(p, "savedialog.timeout", SAVEDIALOG_TIMEOUT);
			
			LANGUAGES 		= p.getProperty("languages").split(",");
			
			BLINK_DELAY_MS = getIntValue(p, "record.blink.delay", BLINK_DELAY_MS);
			
	    	logger.info("reading config file finished");
		} catch (Exception e) {
			logger.error("Error reading config file", e);
			e.printStackTrace();
		}
    	currentTimer = IDLE_TIMEOUT;
    }

    public static void setWebcam(Webcam webcam) {
        if (webcam == null) {
            logger.warn("Webcam set to null");
            return;
        }
        TruthBooth.webcam = webcam;
        webcam.setCustomViewSizes(new Dimension[]{new Dimension(VIDEO_WIDTH, VIDEO_HEIGHT)});
        webcam.setViewSize(new Dimension(VIDEO_WIDTH, VIDEO_HEIGHT));
        webcam.open();
    }

    public static void setMicLine(TargetDataLine line) {
        if (line == null) {
            logger.warn("Mic set to null");
            return;
        }
        TruthBooth.line = line;
    }

    public static class MicInfo {

        public final TargetDataLine line;
        public final String desc;

        public MicInfo(TargetDataLine line, String desc) {
            this.line = line;
            this.desc = desc;
        }
    }

	public static void setLanguage(String lang) {
        logger.info("Setting language to " + lang);
		userInformation.setLanguage(lang);
		frame.addScreens();
		TruthBooth.showScreen(IntroScreen.SCREEN_ID);
	}
	
	public static void resetScreens() {
        logger.info("Resetting screens");
		frame.removeScreens();
		TBButton.clearButtonCache();
	}

	private static Timer idleTimer = new Timer();
	private static TimerTask idleTask = null;

	public static void setTimeout(ResetTimer timer) {
		timer.Set();
		logger.info("Setting timeout to " + timer.toString() + ": " + currentTimer);
		resetIdleTimer();
	}
	
	public static void resetIdleTimer() {
		logger.debug("Resetting timer: " + currentTimer);
		if (idleTask != null) {
			idleTask.cancel();
		}
		if (currentTimer == NO_TIMEOUT) {
			logger.debug("Reset timer disabled");
			return;
		}
		idleTask = new TimerTask() {
			@Override
			public void run() {
				logger.info("Timeout without action, restarting");
				exit();
			}
		};
		idleTimer.schedule(idleTask, currentTimer * 1000);
	}
	
	public static void exit() {
		logger.info("Exiting normally");
		System.exit(0);
	}
}
