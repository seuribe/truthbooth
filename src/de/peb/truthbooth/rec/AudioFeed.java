package de.peb.truthbooth.rec;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class AudioFeed extends Feed {

	private TargetDataLine audioLine;

	public AudioFeed(TargetDataLine audioLine) {
		super(10);
		this.audioLine = audioLine;
		try {
	        if (!audioLine.isOpen()) {
	            audioLine.open();
	        }
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void init() {
        if (!audioLine.isRunning()) {
            audioLine.start();
        }
	}

	@Override
	protected void onPause() {
		audioLine.stop();
	}
	
	@Override
	protected void onResume() {
		audioLine.start();
		AudioProcessing.emptyBuffer(audioLine);
	}
	
	@Override
	protected void deinit() {
		audioLine.stop();
	}

	@Override
	protected Object feed() {
		int available = audioLine.available();
		if (available > 0) {
			short[] buffer = AudioProcessing.readAudio(audioLine);
			return new AudioSample(buffer, getElapsed());
		}
		return null;
	}

}
