package de.peb.truthbooth.ui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.peb.truthbooth.rec.AudioSample;

/**
 * Shows the audio wave as time goes by
 * @author Sebastian
 *
 */
public class AudioMonitoringPanel extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;
	private JProgressBar pbar;
	private float level;

	public AudioMonitoringPanel() {
		pbar = new JProgressBar(JProgressBar.VERTICAL, 0, 100);
		add(pbar);
	}
	
	public void setAudioSample(short[] audioSample) {
		long sum = 0;
		for (short s : audioSample) {
			sum += (s < 0) ? -s : s;
		}
		level = (float)sum / audioSample.length;
		pbar.setValue((int)(level * 100 / Short.MAX_VALUE));
		
		repaint();
	}

	@Override
	public void update(Observable o, Object obj) {
		if (!(obj instanceof AudioSample)) {
			return;
		}
		setAudioSample(((AudioSample)obj).buffer);
	}
}
