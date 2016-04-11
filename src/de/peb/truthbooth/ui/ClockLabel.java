package de.peb.truthbooth.ui;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.peb.truthbooth.TruthBooth;

/**
 * Shows the remaining time for the recording
 * @author Sebastian
 *
 */
public class ClockLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	private Timer timer;
	private TimerTask refreshTask;
	private TimerTask endTask;
    private static final Logger logger = LoggerFactory.getLogger("TruthBooth.Clock");

	public ClockLabel() {
		Font font = new Font("Century Gothic", Font.PLAIN, 32);
		setFont(font);
		setBackground(Color.white);
		setForeground(Color.gray);
		timer = new Timer();
		setSize(150, 100);
	}
	
	private void setRemaining(long millis) {
		int secs = (int) (millis/1000);
		int mins = secs/60;
		secs -= (mins * 60);

		Date date = new Date(millis);
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		setText(sdf.format(date));
		repaint();
	}
	
	private Runnable onEnd;
	private long remaining;
	
	public void startCountdown(long millis, final Runnable onEnd) {
		if (millis <= 0) {
			return;
		}
    	logger.debug("clock Start Countdown");
		final long endTime = System.currentTimeMillis() + millis;
		this.onEnd = onEnd;
		refreshTask = new TimerTask() {
			@Override
			public void run() {
				remaining = endTime - System.currentTimeMillis();
				if (remaining <= 0) {
					setRemaining(0);
				} else {
					setRemaining(remaining);
					TruthBooth.resetIdleTimer();
				}
			}
		};
		timer.schedule(refreshTask, 0, 100);
		
		endTask = new TimerTask() {
			@Override
			public void run() {
				stopCountdown();
				onEnd.run();
			}
		};
		timer.schedule(endTask, new Date(endTime));
	}

	public void pause() {
    	logger.debug("clock pause");

		if (refreshTask != null) {
			refreshTask.cancel();
			refreshTask = null;
		}
		if (endTask != null) {
			endTask.cancel();
			endTask = null;
		}
	}
	
	public void resume() {
    	logger.debug("clock resume");

		startCountdown(remaining, onEnd);
	}
	
	public void stopCountdown() {
    	logger.debug("clock stop Countdown");

		if (refreshTask != null) {
			refreshTask.cancel();
			refreshTask = null;
		}
		if (endTask != null) {
			endTask.cancel();
			endTask = null;
		}
	}
}
